"""Payment guard: should we ask one question before the person pays?

In "digital arrest" and similar frauds no code is shared. The victim opens a
payment app and sends the money while the fraudster is still on the call.
"""

# Package names are from memory and should be re-checked against the Play Store.
PAYMENT_APPS = {
    "com.google.android.apps.nbu.paisa.user": "Google Pay",
    "com.phonepe.app": "PhonePe",
    "net.one97.paytm": "Paytm",
    "in.org.npci.upiapp": "BHIM",
    "com.dreamplug.androidapp": "CRED",
    "com.sbi.lotusintouch": "YONO SBI",
    "com.snapwork.hdfc": "HDFC Bank",
    "com.csam.icici.bank.imobile": "iMobile",
    "com.axis.mobile": "Axis Mobile",
    "com.msf.kbank.mobile": "Kotak Bank",
}


def decide_payment(foreground_package, on_call, caller_known):
    """Return a dict: level, reason, app.

    level is "ask" when a payment app is open during a call with someone who is
    not a saved contact, otherwise "none". A call with family never asks.
    """
    app = PAYMENT_APPS.get(foreground_package or "")
    if app and on_call and not caller_known:
        return {"level": "ask", "reason": "payment_during_unknown_call", "app": app}
    return {"level": "none", "reason": None, "app": app}
