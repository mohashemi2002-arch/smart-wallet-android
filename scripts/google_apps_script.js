/**
 * Google Apps Script Backend for Smart Wallet Android (پولکی / کیف پول هوشمند)
 * Web App Endpoint handling "sync", "register", "signup", "restore", "recover", and "login" requests.
 */

function doGet(e) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var action = e && e.parameter ? e.parameter.action : null;

  if (action === "restore" || action === "recover" || action === "login") {
    return handleRestore(ss, {
      phoneNumber: e.parameter.phoneNumber,
      firstName: e.parameter.firstName,
      lastName: e.parameter.lastName,
      username: e.parameter.username
    });
  }

  return createJsonResponse({
    success: true,
    status: "ok",
    message: "سرور فضای ابری پولکی با موفقیت فعال و آماده به کار است!"
  });
}

function doPost(e) {
  try {
    var rawContent = e && e.postData ? e.postData.contents : "";
    if (!rawContent) {
      return createJsonResponse({
        success: false,
        status: "error",
        message: "درخواست بدون بدنه ارسال شده است."
      });
    }

    var payload = JSON.parse(rawContent);
    var action = payload.action || "sync";
    var ss = SpreadsheetApp.getActiveSpreadsheet();

    if (action === "sync" || action === "register" || action === "signup") {
      return handleSync(ss, payload);
    } else if (action === "restore" || action === "recover" || action === "login") {
      return handleRestore(ss, payload);
    } else {
      return createJsonResponse({
        success: false,
        status: "error",
        message: "عملیات پشتیبانی نشده: " + action
      });
    }
  } catch (err) {
    return createJsonResponse({
      success: false,
      status: "error",
      message: "خطا در پردازش اطلاعات فضای ابری: " + err.toString()
    });
  }
}

function normalizePhone(phone) {
  if (!phone) return "";
  var str = phone.toString().trim();
  var clean = str.replace(/[^0-9]/g, "");
  if (!clean) return "";
  if (clean.indexOf("0098") === 0) clean = "0" + clean.substring(4);
  else if (clean.indexOf("98") === 0 && clean.length === 12) clean = "0" + clean.substring(2);
  else if (clean.indexOf("9") === 0 && clean.length === 10) clean = "0" + clean;
  else if (clean.indexOf("0") !== 0) clean = "0" + clean;
  return clean;
}

function handleSync(ss, payload) {
  var userObj = payload.user || {};
  var rawPhone = userObj.phoneNumber || payload.phoneNumber || "";
  var username = (userObj.username || payload.username || "").toString().trim();
  var phone = normalizePhone(rawPhone) || username;

  var firstName = (userObj.firstName || payload.firstName || "").toString().trim();
  var lastName = (userObj.lastName || payload.lastName || "").toString().trim();
  var passwordHash = (userObj.passwordHash || payload.passwordHash || "").toString().trim();
  var pinHash = (userObj.pinHash || payload.pinHash || "").toString().trim();

  if (!phone && !username) {
    return createJsonResponse({
      success: false,
      status: "error",
      message: "شماره همراه یا نام کاربری جهت همگام‌سازی ابری الزامی است."
    });
  }

  // 1. Update or Insert User in 'Users' sheet
  var userSheet = getOrCreateSheet(ss, "Users", [
    "Phone", "FirstName", "LastName", "Username", "PasswordHash", "PinHash", "LastSync"
  ]);

  var userRows = userSheet.getDataRange().getValues();
  var userFoundIndex = -1;
  for (var i = 1; i < userRows.length; i++) {
    var rowPhone = normalizePhone(userRows[i][0]) || String(userRows[i][0] || "").trim();
    var rowUsername = String(userRows[i][3] || "").trim();

    if ((phone && rowPhone === phone) || (username && rowUsername.toLowerCase() === username.toLowerCase())) {
      userFoundIndex = i + 1;
      break;
    }
  }

  var nowStr = Utilities.formatDate(new Date(), "Asia/Tehran", "yyyy-MM-dd HH:mm:ss");
  var finalKeyPhone = phone;

  if (userFoundIndex > 0) {
    var existingRow = userRows[userFoundIndex - 1];
    finalKeyPhone = normalizePhone(existingRow[0]) || phone || String(existingRow[0] || "").trim();
    var finalFirstName = firstName || existingRow[1] || "";
    var finalLastName = lastName || existingRow[2] || "";
    var finalUsername = username || existingRow[3] || "";
    var finalPassHash = passwordHash || existingRow[4] || "";
    var finalPinHash = pinHash || existingRow[5] || "";

    userSheet.getRange(userFoundIndex, 1, 1, 7).setValues([[
      finalKeyPhone,
      finalFirstName,
      finalLastName,
      finalUsername,
      finalPassHash,
      finalPinHash,
      nowStr
    ]]);
  } else {
    userSheet.appendRow([
      finalKeyPhone,
      firstName,
      lastName,
      username,
      passwordHash,
      pinHash,
      nowStr
    ]);
  }

  // 2. Sync Cards
  var cards = payload.cards || [];
  if (Array.isArray(cards) && cards.length > 0) {
    var cardSheet = getOrCreateSheet(ss, "Cards", [
      "Phone", "CardId", "BankName", "AccountName", "CardNumber", "AccountNumber",
      "BalanceToman", "ColorHex", "Shortcode", "AccountType", "ReadSms", "InitialBal"
    ]);
    removeRowsForUserKey(cardSheet, finalKeyPhone);

    var cardRows = [];
    for (var c = 0; c < cards.length; c++) {
      var card = cards[c];
      var bal = card.balanceToman !== undefined ? card.balanceToman : (card.currentBalance || 0);
      cardRows.push([
        finalKeyPhone,
        String(card.id || ""),
        String(card.bankName || ""),
        String(card.accountName || ""),
        String(card.cardNumber || ""),
        String(card.accountNumber || ""),
        Number(bal) || 0,
        String(card.cardColorHex || "#1E88E5"),
        String(card.smsShortcode || ""),
        String(card.accountType || "BANK_CARD"),
        card.readSmsEnabled !== false,
        card.initialBalanceEnabled !== false
      ]);
    }
    if (cardRows.length > 0) {
      cardSheet.getRange(cardSheet.getLastRow() + 1, 1, cardRows.length, cardRows[0].length).setValues(cardRows);
    }
  }

  // 3. Sync Transactions
  var txs = payload.transactions || [];
  if (Array.isArray(txs) && txs.length > 0) {
    var txSheet = getOrCreateSheet(ss, "Transactions", [
      "Phone", "TxId", "AmountToman", "Type", "CategoryName", "MerchantName",
      "CardNumber", "DateMillis", "Status", "Notes", "BankName", "BalanceAfterToman", "Sender"
    ]);
    removeRowsForUserKey(txSheet, finalKeyPhone);

    var txRows = [];
    for (var t = 0; t < txs.length; t++) {
      var tx = txs[t];
      var amt = tx.amountToman !== undefined ? tx.amountToman : (tx.amount || 0);
      var balAfter = tx.balanceAfterToman !== undefined ? tx.balanceAfterToman : (tx.balanceAfter || 0);
      txRows.push([
        finalKeyPhone,
        String(tx.id || ""),
        Number(amt) || 0,
        String(tx.type || "EXPENSE"),
        String(tx.categoryName || "متفرقه"),
        String(tx.merchantName || ""),
        String(tx.cardNumber || ""),
        tx.dateMillis || new Date().getTime(),
        String(tx.status || "CONFIRMED"),
        String(tx.notes || tx.rawSms || ""),
        String(tx.bankName || ""),
        Number(balAfter) || 0,
        String(tx.senderNumber || "")
      ]);
    }
    if (txRows.length > 0) {
      txSheet.getRange(txSheet.getLastRow() + 1, 1, txRows.length, txRows[0].length).setValues(txRows);
    }
  }

  return createJsonResponse({
    success: true,
    status: "success",
    message: "همگام‌سازی با فضای ابری با موفقیت ذخیره شد.",
    syncedCards: cards.length,
    syncedTransactions: txs.length
  });
}

function handleRestore(ss, payload) {
  var userObj = payload.user || {};
  var rawPhone = payload.phoneNumber || userObj.phoneNumber || "";
  var cleanPhone = normalizePhone(rawPhone);
  var cleanUsername = String(payload.username || userObj.username || "").trim();

  if (!cleanPhone && !cleanUsername) {
    return createJsonResponse({
      success: false,
      status: "error",
      message: "شماره همراه یا نام کاربری جهت بازیابی الزامی است."
    });
  }

  // 1. Fetch User Profile & Credential Hashes
  var userSheet = ss.getSheetByName("Users");
  var userResult = null;
  var targetKey = cleanPhone || cleanUsername;

  if (userSheet) {
    var userRows = userSheet.getDataRange().getValues();
    for (var i = 1; i < userRows.length; i++) {
      var rowPhone = normalizePhone(userRows[i][0]) || String(userRows[i][0] || "").trim();
      var rowUsername = String(userRows[i][3] || "").trim();

      var matchPhone = cleanPhone && rowPhone === cleanPhone;
      var matchUser = cleanUsername && (rowUsername.toLowerCase() === cleanUsername.toLowerCase() || rowPhone === normalizePhone(cleanUsername));

      if (matchPhone || matchUser) {
        targetKey = rowPhone || rowUsername;
        userResult = {
          phoneNumber: rowPhone,
          firstName: String(userRows[i][1] || ""),
          lastName: String(userRows[i][2] || ""),
          username: rowUsername,
          passwordHash: String(userRows[i][4] || ""),
          pinHash: String(userRows[i][5] || "")
        };
        break;
      }
    }
  }

  if (!userResult) {
    return createJsonResponse({
      success: false,
      status: "not_found",
      message: "کاربری با این مشخصات در فضای ابری یافت نشد."
    });
  }

  // 2. Fetch Cards
  var cardSheet = ss.getSheetByName("Cards");
  var restoredCards = [];
  if (cardSheet) {
    var cardRows = cardSheet.getDataRange().getValues();
    for (var c = 1; c < cardRows.length; c++) {
      var cPhone = normalizePhone(cardRows[c][0]) || String(cardRows[c][0] || "").trim();
      if (cPhone === targetKey) {
        var bal = Number(cardRows[c][6]) || 0;
        restoredCards.push({
          id: cardRows[c][1],
          bankName: String(cardRows[c][2] || ""),
          accountName: String(cardRows[c][3] || ""),
          cardNumber: String(cardRows[c][4] || ""),
          accountNumber: String(cardRows[c][5] || ""),
          balanceToman: bal,
          currentBalance: bal,
          cardColorHex: String(cardRows[c][7] || "#1E88E5"),
          smsShortcode: String(cardRows[c][8] || ""),
          accountType: String(cardRows[c][9] || "BANK_CARD"),
          readSmsEnabled: cardRows[c][10] !== false,
          initialBalanceEnabled: cardRows[c][11] !== false
        });
      }
    }
  }

  // 3. Fetch Transactions
  var txSheet = ss.getSheetByName("Transactions");
  var restoredTxs = [];
  if (txSheet) {
    var txRows = txSheet.getDataRange().getValues();
    for (var t = 1; t < txRows.length; t++) {
      var tPhone = normalizePhone(txRows[t][0]) || String(txRows[t][0] || "").trim();
      if (tPhone === targetKey) {
        var amt = Number(txRows[t][2]) || 0;
        var balAfter = Number(txRows[t][11]) || 0;
        var dMillis = Number(txRows[t][7]) || new Date().getTime();
        restoredTxs.push({
          id: txRows[t][1],
          amountToman: amt,
          amount: amt,
          type: String(txRows[t][3] || "EXPENSE"),
          categoryName: String(txRows[t][4] || "متفرقه"),
          merchantName: String(txRows[t][5] || ""),
          cardNumber: String(txRows[t][6] || ""),
          dateMillis: dMillis,
          dateTime: String(dMillis),
          status: String(txRows[t][8] || "CONFIRMED"),
          notes: String(txRows[t][9] || ""),
          rawSms: String(txRows[t][9] || ""),
          bankName: String(txRows[t][10] || ""),
          balanceAfterToman: balAfter,
          balanceAfter: balAfter,
          senderNumber: String(txRows[t][12] || "")
        });
      }
    }
  }

  return createJsonResponse({
    success: true,
    status: "success",
    user: userResult,
    username: userResult.username,
    passwordHash: userResult.passwordHash,
    pinHash: userResult.pinHash,
    cards: restoredCards,
    transactions: restoredTxs
  });
}

function getOrCreateSheet(ss, sheetName, headers) {
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    sheet = ss.insertSheet(sheetName);
    sheet.appendRow(headers);
    sheet.setFrozenRows(1);
  } else {
    // Always keep headers updated in case new columns were added
    sheet.getRange(1, 1, 1, headers.length).setValues([headers]);
  }
  // Prevent stripping leading zeros on phones and card numbers
  sheet.getRange("A:G").setNumberFormat("@");
  return sheet;
}

function removeRowsForUserKey(sheet, key) {
  if (!key) return;
  var rows = sheet.getDataRange().getValues();
  for (var i = rows.length - 1; i >= 1; i--) {
    var rowKey = normalizePhone(rows[i][0]) || String(rows[i][0] || "").trim();
    if (rowKey === key) {
      sheet.deleteRow(i + 1);
    }
  }
}

function createJsonResponse(obj) {
  return ContentService.createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
}
