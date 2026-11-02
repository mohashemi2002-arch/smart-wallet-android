import subprocess

def git(*args):
    return subprocess.check_output(["git", *args])

data = git("ls-tree", "-r", "-z", "--name-only", "FETCH_HEAD")
paths = [p for p in data.split(b"\0") if p]

print("Total tracked files:", len(paths))

suspects = []

for p in paths:
    parts = p.split(b"/")

    suspicious = (
        b"\n" in p
        or b"\r" in p
        or any(len(part) > 200 for part in parts)
        or len(p) > 240
        or b'content":' in p
        or b'package ' in p
    )

    if suspicious or b"Type.kt" in p or b"SettingsViewModel.kt" in p:
        suspects.append(p)

print("\nSuspicious paths found:", len(suspects))

for i, p in enumerate(suspects, 1):
    print(f"\n[{i}] Length: {len(p)} bytes")
    print("Path:", repr(p[:500]))
    if len(p) > 500:
        print("... [truncated]")

print("\nScan completed. No files were changed.")
