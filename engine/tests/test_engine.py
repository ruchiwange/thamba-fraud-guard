"""Run from the engine folder:  python -m unittest discover -s tests -v"""

import csv
import os
import sys
import unittest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from thamba_engine import on_link, on_sms  # noqa: E402
from thamba_engine.otp import classify_sms  # noqa: E402
from thamba_engine.sender import parse_sender  # noqa: E402

SAMPLES = os.path.join(os.path.dirname(__file__), "..", "samples", "sms_samples.csv")


class SampleMessages(unittest.TestCase):
    """Every row of samples/sms_samples.csv must get the verdict written beside it."""

    def test_all_samples(self):
        with open(SAMPLES, encoding="utf-8", newline="") as handle:
            rows = list(csv.DictReader(handle))
        self.assertGreater(len(rows), 10)
        for row in rows:
            with self.subTest(sample=row["id"]):
                result = on_sms(
                    row["sender"],
                    row["text"],
                    on_call=row["on_call"] == "yes",
                    caller_known=row["caller_known"] == "yes",
                )
                self.assertEqual(result["code"]["level"], row["expect_code_level"])
                self.assertEqual(result["code"]["kind"] or "", row["expect_code_kind"])
                self.assertEqual(result["sender"]["level"], row["expect_sender_level"])
                self.assertEqual(result["link"]["level"], row["expect_link_level"])


class CodeGuard(unittest.TestCase):
    BANK = "OTP is 482913 for txn of Rs.25,000.00 at AMAZON PAY on HDFC Bank card ending 4421."

    def test_alert_needs_all_three_conditions(self):
        self.assertEqual(on_sms("VM-HDFCBK-T", self.BANK, True, False)["code"]["level"], "alert")
        self.assertEqual(on_sms("VM-HDFCBK-T", self.BANK, False, False)["code"]["level"], "notice")
        self.assertEqual(on_sms("VM-HDFCBK-T", self.BANK, True, True)["code"]["level"], "notice")

    def test_amount_is_read_from_the_message(self):
        self.assertEqual(classify_sms(self.BANK)["amount"], 25000.0)

    def test_delivery_code_never_alerts(self):
        text = "Your delivery OTP is 771204. Share it with the delivery agent."
        self.assertEqual(on_sms("AX-AMAZON-S", text, True, False)["code"]["level"], "notice")

    def test_message_without_code_is_ignored(self):
        self.assertEqual(on_sms("VM-ICICIB-T", "Rs 1,200 debited from A/c XX8891.", True, False)["code"]["level"], "none")


class LinkGuard(unittest.TestCase):
    def test_official_government_site_is_green(self):
        self.assertEqual(on_link("https://echallan.parivahan.gov.in")["level"], "green")

    def test_lookalike_is_red_and_offers_the_real_site(self):
        verdict = on_link("https://echallan.parivahan.in/pay")
        self.assertEqual(verdict["level"], "red")
        self.assertEqual(verdict["real_site"], "echallan.parivahan.gov.in")

    def test_gov_in_inside_a_longer_name_is_not_official(self):
        self.assertNotEqual(on_link("https://parivahan.gov.in.pay-fine.com/")["level"], "green")

    def test_free_offer_is_red(self):
        self.assertEqual(on_link("free-iphone15-offer.xyz")["level"], "red")

    def test_app_file_is_red(self):
        self.assertEqual(on_link("http://example.com/files/RTO_Challan.apk")["level"], "red")

    def test_ordinary_site_is_neutral(self):
        self.assertEqual(on_link("https://www.wikipedia.org/")["level"], "neutral")

    def test_shortened_link_is_amber(self):
        self.assertEqual(on_link("https://bit.ly/3xYzAbC")["level"], "amber")


class MessageGuard(unittest.TestCase):
    def test_sender_formats(self):
        self.assertTrue(parse_sender("+919812345610")["is_mobile"])
        header = parse_sender("VM-HDFCBK-T")
        self.assertEqual((header["name"], header["kind"]), ("HDFCBK", "T"))

    def test_friend_texting_from_mobile_is_not_flagged(self):
        result = on_sms("9812345610", "Reached home safely, will call you tomorrow.")
        self.assertEqual(result["sender"]["level"], "none")


if __name__ == "__main__":
    unittest.main()
