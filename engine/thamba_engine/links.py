"""Link guard: give a tapped link a red, amber, green or neutral verdict.

Rules only, no internet lookup. An online safe-browsing check can be added
later; these rules must keep working when the phone is offline.
"""

import re
from urllib.parse import urlparse

# Government and banking suffixes that only genuine bodies can register.
_OFFICIAL_SUFFIXES = (".gov.in", ".nic.in", ".bank.in")

# Names fraudsters imitate, with the genuine site to offer instead.
# Small on purpose: every entry here must be checked by hand.
_PROTECTED = {
    "echallan": "echallan.parivahan.gov.in",
    "parivahan": "parivahan.gov.in",
    "uidai": "uidai.gov.in",
    "aadhaar": "uidai.gov.in",
    "incometax": "incometax.gov.in",
    "pmkisan": "pmkisan.gov.in",
    "epfo": "epfindia.gov.in",
    "digilocker": "digilocker.gov.in",
    "cybercrime": "cybercrime.gov.in",
}

_BAIT = re.compile(
    r"(free|offer|gift|prize|winner|lucky|reward|cashback|claim|iphone|kyc[-_]?update|update[-_]?kyc|recharge[-_]?free)",
    re.IGNORECASE,
)
_SHORTENERS = {"bit.ly", "tinyurl.com", "cutt.ly", "t.co", "is.gd", "rb.gy", "goo.gl", "tiny.cc", "shorturl.at"}
_RISKY_TLDS = (".xyz", ".top", ".click", ".live", ".icu", ".buzz", ".cfd", ".sbs", ".rest", ".monster")
_IP_HOST = re.compile(r"^\d{1,3}(\.\d{1,3}){3}$")


def _host_of(url):
    url = (url or "").strip()
    if "://" not in url:
        url = "http://" + url
    parsed = urlparse(url)
    host = (parsed.hostname or "").lower().rstrip(".")
    if host.startswith("www."):
        host = host[4:]
    return host, parsed


def is_official(host):
    return any(host == s[1:] or host.endswith(s) for s in _OFFICIAL_SUFFIXES)


def check_url(url):
    """Return a dict: level, reason, host, real_site.

    level is "red" (block), "amber" (open with a caution), "green" (official
    site) or "neutral" (nothing known against it; open normally).
    """
    host, parsed = _host_of(url)
    result = {"level": "neutral", "reason": "link_unknown", "host": host, "real_site": None}
    if not host:
        result.update(level="amber", reason="link_unreadable")
        return result

    if is_official(host):
        result.update(level="green", reason="link_official")
        return result

    path = (parsed.path or "").lower()
    if path.endswith(".apk"):
        result.update(level="red", reason="link_app_file")
        return result

    for name, real in _PROTECTED.items():
        if name in host:
            result.update(level="red", reason="link_lookalike", real_site=real)
            return result

    if _IP_HOST.match(host):
        result.update(level="red", reason="link_ip_address")
        return result

    if _BAIT.search(host) or _BAIT.search(path):
        result.update(level="red", reason="link_bait")
        return result

    if host in _SHORTENERS:
        result.update(level="amber", reason="link_shortened")
    elif host.startswith("xn--") or ".xn--" in host:
        result.update(level="amber", reason="link_disguised_letters")
    elif host.endswith(_RISKY_TLDS):
        result.update(level="amber", reason="link_risky_ending")
    elif parsed.scheme == "http":
        result.update(level="amber", reason="link_not_secure")
    return result


_URL_IN_TEXT = re.compile(r"(?:https?://|www\.)[^\s<>\"']+|\b[a-z0-9-]+(?:\.[a-z0-9-]+)+/[^\s<>\"']*", re.IGNORECASE)


def find_urls(text):
    """Pull the links out of a message."""
    return [u.rstrip(".,)") for u in _URL_IN_TEXT.findall(text or "")]
