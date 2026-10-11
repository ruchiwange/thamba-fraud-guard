"""Code guard: work out whether an SMS carries a one-time code and what the code is for."""

import re

# What a code can do, from most to least dangerous to read out to a caller.
PAYMENT = "payment"      # moves money
LOGIN = "login"          # opens a bank, UPI, wallet or chat account
IDENTITY = "identity"    # Aadhaar, DigiLocker, PAN: lets someone act as you
DELIVERY = "delivery"    # hands over a parcel
OTHER = "other"          # ordinary sign-ups

_OTP_WORDS = re.compile(
    r"\b(otp|one[\s-]?time\s+(password|passcode|pin)|verification\s+code|security\s+code|passcode)\b"
    r"|ओटीपी",
    re.IGNORECASE,
)
# A run of 4 to 8 digits that is not part of a longer number or an amount like 25,000.00
_CODE = re.compile(r"(?<!\d)(?<!\d[.,])\d{4,8}(?![.,]?\d)")
_AMOUNT = re.compile(r"(?:rs\.?|inr|₹)\s*([\d,]+(?:\.\d{1,2})?)", re.IGNORECASE)

_PAYMENT_WORDS = re.compile(
    r"\b(txn|transaction|debit(ed)?|payment|purchase|transfer|beneficiary|upi|card|netbanking|net\s+banking|withdraw(al)?|emi)\b",
    re.IGNORECASE,
)
_IDENTITY_WORDS = re.compile(r"\b(aadhaar|aadhar|uidai|digilocker|e-?kyc|pan\s+card|epfo|uan)\b", re.IGNORECASE)
_LOGIN_WORDS = re.compile(
    r"\b(log\s?in|sign\s?in|register(ing)?\s+(your\s+)?device|new\s+device|whatsapp|account\s+verification|reset\s+(your\s+)?(password|pin|mpin))\b",
    re.IGNORECASE,
)
_DELIVERY_WORDS = re.compile(
    r"\b(deliver(y|ed|ing)?|parcel|package|shipment|courier|order|rider|delivery\s+(agent|partner|executive))\b",
    re.IGNORECASE,
)


def parse_amount(text):
    """Return the rupee amount mentioned in the SMS as a float, or None."""
    match = _AMOUNT.search(text)
    if not match:
        return None
    try:
        return float(match.group(1).replace(",", ""))
    except ValueError:
        return None


def classify_sms(text):
    """Describe the code in an SMS.

    Returns a dict: is_otp, kind, amount. kind is None when the SMS has no code.
    """
    text = text or ""
    if not (_OTP_WORDS.search(text) and _CODE.search(text)):
        return {"is_otp": False, "kind": None, "amount": None}

    amount = parse_amount(text)
    has_delivery = bool(_DELIVERY_WORDS.search(text))

    # Order matters. A payment at a shopping site names the shop, so payment
    # words win over delivery words. An amount alone does not make it a payment,
    # because delivery messages often repeat the order value.
    if _PAYMENT_WORDS.search(text):
        kind = PAYMENT
    elif _IDENTITY_WORDS.search(text):
        kind = IDENTITY
    elif _LOGIN_WORDS.search(text):
        kind = LOGIN
    elif has_delivery:
        kind = DELIVERY
    elif amount is not None:
        kind = PAYMENT
    else:
        kind = OTHER

    return {"is_otp": True, "kind": kind, "amount": amount if kind == PAYMENT else None}


def decide_code(text, on_call, caller_known):
    """Decide how loudly to warn about the code in this SMS.

    level is one of:
      "alert"  - full-screen warning: a dangerous code arrived during a call
                 with someone who is not in the contacts
      "notice" - small notification saying what the code is for
      "none"   - no code in this SMS
    """
    info = classify_sms(text)
    if not info["is_otp"]:
        return {"level": "none", "reason": None, "kind": None, "amount": None}

    dangerous = info["kind"] in (PAYMENT, LOGIN, IDENTITY)
    if dangerous and on_call and not caller_known:
        level, reason = "alert", "code_" + info["kind"] + "_unknown_call"
    else:
        level, reason = "notice", "code_" + info["kind"]
    return {"level": level, "reason": reason, "kind": info["kind"], "amount": info["amount"]}
