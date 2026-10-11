"""The entry points the phone app calls. Everything returned is plain data."""

from .apps import check_app
from .links import check_url, find_urls
from .otp import decide_code
from .payments import decide_payment
from .sender import check_sender

# When a message has several links, report the most worrying one.
_LINK_RANK = {"neutral": 0, "green": 1, "amber": 2, "red": 3}


def on_sms(sender, text, on_call=False, caller_known=True):
    """An SMS has arrived.

    sender        who sent it, exactly as the phone shows it
    text          the message
    on_call       True if a phone call is in progress right now
    caller_known  True if the other person on that call is in the contacts

    Returns a dict with one entry per guard: code, sender, link.
    """
    worst_link = {"level": "neutral", "reason": None, "host": None, "real_site": None}
    for url in find_urls(text):
        verdict = check_url(url)
        if _LINK_RANK[verdict["level"]] > _LINK_RANK[worst_link["level"]]:
            worst_link = verdict

    return {
        "code": decide_code(text, on_call, caller_known),
        "sender": check_sender(sender, text),
        "link": worst_link,
    }


def on_link(url):
    """A link was tapped anywhere on the phone."""
    return check_url(url)


def on_app(package, installer, permissions, is_system=False):
    """One installed app is being checked."""
    return check_app(package, installer, permissions, is_system)


def on_payment(foreground_package, on_call, caller_known):
    """An app has come to the front of the screen."""
    return decide_payment(foreground_package, on_call, caller_known)
