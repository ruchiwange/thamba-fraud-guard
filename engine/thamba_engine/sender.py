"""Message guard: does this SMS claim an authority its sender does not have?"""

import re

_MOBILE = re.compile(r"^(?:\+?91[\s-]?)?[6-9]\d{9}$")
# Registered sender names look like "VM-HDFCBK-S": operator code, name, then a
# letter for the type of message (S service, T transactional, P promotional,
# G government).
_HEADER = re.compile(r"^[A-Z]{2}-([A-Z0-9]{3,9})(?:-([STPG]))?$", re.IGNORECASE)

_AUTHORITY = re.compile(
    r"\b(bank|kyc|electricity|power\s+(supply|connection)|bijli|disconnect(ed|ion)?|challan|traffic\s+police|rto|"
    r"income\s+tax|refund|customs|courier|parcel|police|court|aadhaar|pan\s+card|sim\s+(card\s+)?(block|deactivat)|"
    r"account\s+(block|suspend|freez)|officer|govt|government)\w*",
    re.IGNORECASE,
)
_PHONE_IN_TEXT = re.compile(r"(?<!\d)(?:\+?91[\s-]?)?[6-9]\d{9}(?!\d)")


def parse_sender(sender):
    """Return a dict: is_mobile, name, kind (S/T/P/G or None)."""
    sender = (sender or "").strip()
    if _MOBILE.match(sender.replace(" ", "")):
        return {"is_mobile": True, "name": None, "kind": None}
    match = _HEADER.match(sender)
    if match:
        kind = match.group(2).upper() if match.group(2) else None
        return {"is_mobile": False, "name": match.group(1).upper(), "kind": kind}
    return {"is_mobile": False, "name": sender or None, "kind": None}


def check_sender(sender, text):
    """Return a dict: level, reason.

    "amber" when an ordinary mobile number sends a message that speaks like a
    bank, a government office or a utility. Genuine ones use registered names.
    """
    info = parse_sender(sender)
    text = text or ""
    if info["is_mobile"] and _AUTHORITY.search(text):
        asks_callback = bool(_PHONE_IN_TEXT.search(text)) or "call" in text.lower()
        reason = "sender_mobile_claims_official_callback" if asks_callback else "sender_mobile_claims_official"
        return {"level": "amber", "reason": reason}
    return {"level": "none", "reason": None}
