/**
 * Google Apps Script Backend for Smart Wallet Android (کیف پول هوشمند)
 * Web App Endpoint handling "sync", "register", and "restore" requests.
 *
 * Persists data into Google Sheets:
 * - Users (Columns: Phone, First Name, Last Name, Username, PasswordHash, PinHash, LastSync)
 * - Cards (Columns: Phone, Card ID, Bank Name, Account Name, Card Number, Account Number, Balance Toman, Color Hex, Shortcode, Type, ReadSms, InitialBal)
 * - Transactions (Columns: Phone, Tx ID, Amount Toman, Type, Category, Merchant, Card Number, Date Millis, Status, Notes, Bank Name, Balance After, Sender)
 */

function doPost(e) {
  try {
    var rawContent = e.postData ? e.postData.contents : "";
    if (!rawContent) {
      return ContentService.createTextOutput(JSON.stringify({
        success: false,
        message: "درخواست بدون بدنه ارسال شده است."
      })).setMimeType(ContentService.MimeType.JSON);
    }

    var payload = JSON.parse(rawContent);
    var action = payload.action || "sync";
    var ss = SpreadsheetApp.getActiveSpreadsheet();

    if (action === "sync" || action === "register") {
      return handleSync(ss, payload);
    } else if (action === "restore") {
      return handleRestore(ss, payload);
    } else {
      return ContentService.createTextOutput(JSON.stringify({
        success: false,
        message: "عملیات پشتیبانی نشده: " + action
      })).setMimeType(ContentService.MimeType.JSON);
    }
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({
      success: false,
      message: "خطا در پردازش اطلاعات ابری: " + err.toString()
    })).setMimeType(ContentService.MimeType.JSON);
  }
}

function normalizePhone(phone) {
  if (!phone) return "";
  var clean = phone.toString().replace(/[^0-9]/g, "");
  if (clean.indexOf("0") === 0) return clean;
  if (clean.indexOf("98") === 0) return "0" + clean.substring(2);
  return "0" + clean;
}

function handleSync(ss, payload) {
  var userObj = payload.user || {};
  var phone = normalizePhone(userObj.phoneNumber || payload.phoneNumber);
  var firstName = userObj.firstName || payload.firstName || "";
  var lastName = userObj.lastName || payload.lastName || "";
  var username = userObj.username || payload.username || "";
  var passwordHash = userObj.passwordHash || payload.passwordHash || "";
  var pinHash = userObj.pinHash || payload.pinHash || "";

  if (!phone) {
    return ContentService.createTextOutput(JSON.stringify({
      success: false,
      message: "شماره همراه جهت همگام‌سازی ابری الزامی است."
    })).setMimeType(ContentService.MimeType.JSON);
  }

  // 1. Update or Insert User in 'Users' sheet
  var userSheet = getOrCreateSheet(ss, "Users", [
    "Phone", "FirstName", "LastName", "Username", "PasswordHash", "PinHash", "LastSync"
  ]);

  var userRows = userSheet.getDataRange().getValues();
  var userFoundIndex = -1;
  for (var i = 1; i < userRows.length; i++) {
    if (normalizePhone(userRows[i][0]) === phone) {
      userFoundIndex = i + 1;
      break;
    }
  }

  var nowStr = Utilities.formatDate(new Date(), "Asia/Tehran", "yyyy-MM-dd HH:mm:ss");
  if (userFoundIndex > 0) {
    // Preserve existing hashes if not passed
    var existingRow = userRows[userFoundIndex - 1];
    var finalUsername = username || existingRow[3] || "";
    var finalPassHash = passwordHash || existingRow[4] || "";
    var finalPinHash = pinHash || existingRow[5] || "";

    userSheet.getRange(userFoundIndex, 2, 1, 6).setValues([[
      firstName || existingRow[1],
      lastName || existingRow[2],
      finalUsername,
      finalPassHash,
      finalPinHash,
      nowStr
    ]]);
  } else {
    userSheet.appendRow([
      phone,
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
  if (cards.length > 0) {
    var cardSheet = getOrCreateSheet(ss, "Cards", [
      "Phone", "CardId", "BankName", "AccountName", "CardNumber", "AccountNumber",
      "BalanceToman", "ColorHex", "Shortcode", "AccountType", "ReadSms", "InitialBal"
    ]);
    removeRowsForPhone(cardSheet, phone);

    var cardRows = [];
    for (var c = 0; c < cards.length; c++) {
      var card = cards[c];
      cardRows.push([
        phone,
        card.id || "",
        card.bankName || "",
        card.accountName || "",
        card.cardNumber || "",
        card.accountNumber || "",
        card.balanceToman || 0,
        card.cardColorHex || "#1E88E5",
        card.smsShortcode || "",
        card.accountType || "BANK_CARD",
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
  if (txs.length > 0) {
    var txSheet = getOrCreateSheet(ss, "Transactions", [
      "Phone", "TxId", "AmountToman", "Type", "CategoryName", "MerchantName",
      "CardNumber", "DateMillis", "Status", "Notes", "BankName", "BalanceAfterToman", "Sender"
    ]);
    removeRowsForPhone(txSheet, phone);

    var txRows = [];
    for (var t = 0; t < txs.length; t++) {
      var tx = txs[t];
      txRows.push([
        phone,
        tx.id || "",
        tx.amountToman || 0,
        tx.type || "EXPENSE",
        tx.categoryName || "متفرقه",
        tx.merchantName || "",
        tx.cardNumber || "",
        tx.dateMillis || new Date().getTime(),
        tx.status || "CONFIRMED",
        tx.notes || "",
        tx.bankName || "",
        tx.balanceAfterToman || 0,
        tx.senderNumber || ""
      ]);
    }
    if (txRows.length > 0) {
      txSheet.getRange(txSheet.getLastRow() + 1, 1, txRows.length, txRows[0].length).setValues(txRows);
    }
  }

  return ContentService.createTextOutput(JSON.stringify({
    success: true,
    message: "همگام‌سازی با فضای ابری با موفقیت ذخیره شد.",
    syncedCards: cards.length,
    syncedTransactions: txs.length
  })).setMimeType(ContentService.MimeType.JSON);
}

function handleRestore(ss, payload) {
  var phone = normalizePhone(payload.phoneNumber);
  if (!phone) {
    return ContentService.createTextOutput(JSON.stringify({
      success: false,
      message: "شماره همراه جهت بازیابی الزامی است."
    })).setMimeType(ContentService.MimeType.JSON);
  }

  // 1. Fetch User Profile & Credential Hashes
  var userSheet = ss.getSheetByName("Users");
  var userResult = { phoneNumber: phone };
  if (userSheet) {
    var userRows = userSheet.getDataRange().getValues();
    for (var i = 1; i < userRows.length; i++) {
      if (normalizePhone(userRows[i][0]) === phone) {
        userResult.firstName = userRows[i][1] || "";
        userResult.lastName = userRows[i][2] || "";
        userResult.username = userRows[i][3] || "";
        userResult.passwordHash = userRows[i][4] || "";
        userResult.pinHash = userRows[i][5] || "";
        break;
      }
    }
  }

  // 2. Fetch Cards
  var cardSheet = ss.getSheetByName("Cards");
  var restoredCards = [];
  if (cardSheet) {
    var cardRows = cardSheet.getDataRange().getValues();
    for (var c = 1; c < cardRows.length; c++) {
      if (normalizePhone(cardRows[c][0]) === phone) {
        restoredCards.push({
          id: cardRows[c][1],
          bankName: cardRows[c][2],
          accountName: cardRows[c][3],
          cardNumber: cardRows[c][4],
          accountNumber: cardRows[c][5],
          balanceToman: Number(cardRows[c][6]) || 0,
          cardColorHex: cardRows[c][7] || "#1E88E5",
          smsShortcode: cardRows[c][8],
          accountType: cardRows[c][9] || "BANK_CARD",
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
      if (normalizePhone(txRows[t][0]) === phone) {
        restoredTxs.push({
          id: txRows[t][1],
          amountToman: Number(txRows[t][2]) || 0,
          type: txRows[t][3] || "EXPENSE",
          categoryName: txRows[t][4] || "متفرقه",
          merchantName: txRows[t][5] || "",
          cardNumber: txRows[t][6] || "",
          dateMillis: Number(txRows[t][7]) || new Date().getTime(),
          status: txRows[t][8] || "CONFIRMED",
          notes: txRows[t][9] || "",
          bankName: txRows[t][10] || "",
          balanceAfterToman: Number(txRows[t][11]) || 0,
          senderNumber: txRows[t][12] || ""
        });
      }
    }
  }

  return ContentService.createTextOutput(JSON.stringify({
    success: true,
    user: userResult,
    username: userResult.username,
    passwordHash: userResult.passwordHash,
    pinHash: userResult.pinHash,
    cards: restoredCards,
    transactions: restoredTxs
  })).setMimeType(ContentService.MimeType.JSON);
}

function getOrCreateSheet(ss, sheetName, headers) {
  var sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    sheet = ss.insertSheet(sheetName);
    sheet.appendRow(headers);
    sheet.setFrozenRows(1);
  }
  return sheet;
}

function removeRowsForPhone(sheet, phone) {
  var rows = sheet.getDataRange().getValues();
  for (var i = rows.length - 1; i >= 1; i--) {
    if (normalizePhone(rows[i][0]) === phone) {
      sheet.deleteRow(i + 1);
    }
  }
}
