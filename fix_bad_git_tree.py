import subprocess
from pathlib import Path

BAD_PREFIXES = [
    b'app/src/main/java/com/example/ui/theme/Type.kt\ncontent":"package com.example.ui.theme',
    b'app/src/main/java/com/example/ui/viewmodel/SettingsViewModel.kt\ncontent":"package com.example.ui.viewmodel',
]

HEALTHY_FILES = [
    b'app/src/main/java/com/example/ui/theme/Type.kt',
    b'app/src/main/java/com/example/ui/viewmodel/SettingsViewModel.kt',
    b'app/src/main/java/com/example/ui/screens/SettingsScreen.kt',
    b'app/src/main/java/com/example/ui/screens/SmsSenderSettingsScreen.kt',
]

def git(*args, input_data=None):
    p = subprocess.run(
        ["git", *args],
        input=input_data,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE
    )
    if p.returncode != 0:
        raise RuntimeError(
            f"Git error: {' '.join(args)}\n"
            + p.stderr.decode("utf-8", errors="replace")
        )
    return p.stdout

# بررسی نسخه مبنا
git("rev-parse", "--verify", "FETCH_HEAD")

old_tree = git(
    "rev-parse", "FETCH_HEAD^{tree}"
).strip().decode("ascii")

# بررسی مسیرهای فعلی
original_paths = [
    p for p in git(
        "ls-tree", "-r", "-z", "--name-only", "FETCH_HEAD"
    ).split(b"\0") if p
]

malformed = [
    p for p in original_paths
    if b"\n" in p or b"\r" in p
]

print("Malformed paths found:", len(malformed))

for p in malformed:
    print(repr(p[:250]))

if len(malformed) != 2:
    raise RuntimeError(
        "Expected exactly two malformed paths. "
        "No new commit was created."
    )

for prefix in BAD_PREFIXES:
    if not any(p.startswith(prefix) for p in malformed):
        raise RuntimeError(
            "Expected malformed path not found: " + repr(prefix)
        )

removed = []

def rebuild_tree(tree_oid, parent_path=b""):
    entries = git("ls-tree", "-z", tree_oid).split(b"\0")
    output = []
    changed = False

    for entry in entries:
        if not entry:
            continue

        metadata, name = entry.split(b"\t", 1)
        mode, obj_type, object_id = metadata.split(b" ")
        full_path = parent_path + name

        # حذف فقط دو ورودی خراب و تمام زیرشاخه‌های آن‌ها
        if any(full_path.startswith(prefix) for prefix in BAD_PREFIXES):
            removed.append(full_path)
            changed = True
            continue

        if obj_type == b"tree":
            new_oid, sub_changed = rebuild_tree(
                object_id.decode("ascii"),
                full_path + b"/"
            )

            if sub_changed:
                output.append(
                    mode + b" tree " + new_oid.encode("ascii")
                    + b"\t" + name + b"\0"
                )
                changed = True
            else:
                output.append(entry + b"\0")
        else:
            output.append(entry + b"\0")

    if not changed:
        return tree_oid, False

    new_tree = git(
        "mktree", "-z",
        input_data=b"".join(output)
    ).strip().decode("ascii")

    return new_tree, True


# بازسازی درخت
new_tree, changed = rebuild_tree(old_tree)

if not changed or len(removed) != 2:
    raise RuntimeError(
        "Unexpected removal result. No commit was created."
    )

# اعتبارسنجی درخت جدید قبل از ساخت Commit
new_paths = [
    p for p in git(
        "ls-tree", "-r", "-z", "--name-only", new_tree
    ).split(b"\0") if p
]

remaining_bad = [
    p for p in new_paths
    if b"\n" in p or b"\r" in p
]

missing_healthy = [
    p for p in HEALTHY_FILES
    if p not in new_paths
]

if remaining_bad:
    print("Remaining malformed paths:")
    for p in remaining_bad:
        print(repr(p[:250]))
    raise RuntimeError("Malformed paths still exist.")

if missing_healthy:
    print("Missing healthy files:")
    for p in missing_healthy:
        print(repr(p))
    raise RuntimeError("Healthy file verification failed.")

# ساخت Commit بر پایه آخرین نسخه GitHub
new_commit = git(
    "commit-tree",
    new_tree,
    "-p", "FETCH_HEAD",
    "-m", "Remove malformed Kotlin source paths"
).strip().decode("ascii")

Path("cleanup_commit.txt").write_text(
    new_commit + "\n",
    encoding="ascii"
)

print("\nSUCCESS")
print("Removed malformed entries:", len(removed))
print("Verified healthy files:", len(HEALTHY_FILES))
print("Remaining malformed paths:", len(remaining_bad))
print("New commit:", new_commit)
print("Saved in cleanup_commit.txt")
print("\nNo push performed.")
