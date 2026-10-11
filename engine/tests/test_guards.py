"""Tests for the app guard and the payment guard.

Run from the engine folder:  python -m unittest discover -s tests -v
"""

import json
import os
import sys
import unittest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from thamba_engine import bridge, on_app, on_payment  # noqa: E402

SMS = "android.permission.RECEIVE_SMS"
CAMERA = "android.permission.CAMERA"
PLAY = "com.android.vending"
GPAY = "com.google.android.apps.nbu.paisa.user"


class AppGuard(unittest.TestCase):
    def test_sideloaded_app_that_reads_sms_is_red(self):
        verdict = on_app("com.rto.challan.view", "", [SMS, CAMERA])
        self.assertEqual((verdict["level"], verdict["reason"]), ("red", "app_sideloaded_reads_sms"))

    def test_same_app_from_the_play_store_is_not_flagged(self):
        self.assertEqual(on_app("com.some.messenger", PLAY, [SMS])["level"], "none")

    def test_sideloaded_app_without_sms_access_is_not_flagged(self):
        self.assertEqual(on_app("com.some.game", "", [CAMERA])["level"], "none")

    def test_app_that_came_with_the_phone_is_not_flagged(self):
        self.assertEqual(on_app("com.android.mms", "", [SMS], is_system=True)["level"], "none")

    def test_remote_access_app_is_amber_even_from_the_play_store(self):
        verdict = on_app("com.anydesk.anydeskandroid", PLAY, [])
        self.assertEqual((verdict["level"], verdict["name"]), ("amber", "AnyDesk"))


class PaymentGuard(unittest.TestCase):
    def test_asks_when_paying_during_a_call_with_a_stranger(self):
        verdict = on_payment(GPAY, on_call=True, caller_known=False)
        self.assertEqual((verdict["level"], verdict["app"]), ("ask", "Google Pay"))

    def test_silent_when_family_is_on_the_call(self):
        self.assertEqual(on_payment(GPAY, on_call=True, caller_known=True)["level"], "none")

    def test_silent_when_no_call_is_on(self):
        self.assertEqual(on_payment(GPAY, on_call=False, caller_known=False)["level"], "none")

    def test_silent_for_an_app_that_is_not_a_payment_app(self):
        self.assertEqual(on_payment("com.whatsapp", on_call=True, caller_known=False)["level"], "none")


class Bridge(unittest.TestCase):
    """The Android app only ever sees these JSON strings."""

    def test_app_bridge_splits_the_permission_list(self):
        verdict = json.loads(bridge.on_app_json("com.rto.challan.view", "", SMS + "," + CAMERA, False))
        self.assertEqual(verdict["level"], "red")

    def test_payment_bridge(self):
        self.assertEqual(json.loads(bridge.on_payment_json(GPAY, True, False))["level"], "ask")


if __name__ == "__main__":
    unittest.main()
