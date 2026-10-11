"""Thamba decision engine.

Pure Python, standard library only. It takes plain facts about what just
happened on the phone (an SMS arrived, a link was tapped, a call is on) and
returns a verdict. It never reads the phone itself, so the same code can be
tested on a laptop and called from the Android app.
"""

from .engine import on_app, on_link, on_payment, on_sms

__all__ = ["on_sms", "on_link", "on_app", "on_payment"]
