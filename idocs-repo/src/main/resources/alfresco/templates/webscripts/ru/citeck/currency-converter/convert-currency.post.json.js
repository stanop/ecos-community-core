(function () {
    var fromCurrencyRef = json.get('fromCurrencyRef');
    var toCurrencyRef = json.get('toCurrencyRef');
    var amount = json.get('amount');
    if (!fromCurrencyRef || !toCurrencyRef || !amount) {
        status.setCode(status.STATUS_BAD_ARGUMENT, "Missing mandatory argument.");
        return;
    }
    var convertedAmount = 0;
    convertedAmount = currencyService.transfer(fromCurrencyRef, toCurrencyRef, amount);
    if (convertedAmount) {
        model.data = convertedAmount;
    } else {
        model.data = "error";
    }
})();