# Testing

## What has been tested

### Engine (Python)

25 unit tests cover the code, link, message, app and payment rules. Run them with:

```
cd engine
python -m unittest discover -s tests -v
```

### App, on an Android emulator

The app was built from this repository and run on an Android 14 emulator (Pixel 6 profile) on 12 October 2026. Calls and SMS were simulated with the emulator's own tools. Each row below was run once.

| # | Situation simulated | Expected | Result |
|---|---|---|---|
| 1 | Call from a number not in contacts, answered, then a bank OTP SMS for a ₹25,000 transaction | Red full-screen warning in Marathi showing the amount | Passed. Warning appeared 0.6 seconds after the SMS |
| 2 | "Cut the call" tapped on that warning | Call ends, warning closes | Passed |
| 3 | Same call, then a delivery OTP SMS | No red screen; small note explaining the code | Passed |
| 4 | Bank OTP SMS with no call in progress | No red screen; small note | Passed |
| 5 | Call from a saved contact, answered, then a bank OTP SMS | No red screen | Passed |
| 6 | SMS from a ten-digit mobile number claiming an electricity disconnection | Warning notification | Passed |
| 7 | Link `echallan.parivahan.in/pay` opened | Red screen offering the real site | Passed |
| 8 | Link `free-iphone15-offer.xyz/claim` opened | Red screen | Passed |
| 9 | Link `bit.ly/...` opened | Amber screen | Passed |
| 10 | Link `echallan.parivahan.gov.in` opened | Passed to the browser | Passed |
| 11 | Link `wikipedia.org` opened | Passed to the browser | Passed |
| 12 | Unknown caller on the call, then a payment app opened | Amber question | Passed |
| 13 | Saved contact on the call, then a payment app opened | No question | Passed |
| 14 | "Check my phone for dangerous apps" | Lists an app installed outside a store that reads SMS; no system apps listed | Passed |
| 15 | "Money gone?" helper filled in | Statement and three steps shown | Passed |
| 16 | Language switched to English | Screens change language | Passed |

For rows 12 to 14 no real payment app was available on the emulator. A copy of this app was installed under the package name of a payment app to stand in for one.

### Problems found by this testing, and fixed

- The app froze ("isn't responding") when the warning opened, because the voice setup and the Python engine ran on the main thread. Both now run on background threads.
- The first warning after the app had been idle took about 20 seconds, because Python was starting. The engine now starts when the phone rings, before any SMS arrives.
- The stand-in notification for a warning stayed on screen after the warning was closed. It is now cleared.

## What has not been tested

- **A real phone.** Everything above ran on an emulator. Real phones from different makers restrict background apps in different ways.
- **The spoken warning.** The emulator had no audio. Whether the Marathi and Hindi voices are present and audible during a call is unknown.
- **Hindi screens.** Only Marathi and English were looked at.
- **Outgoing calls,** dual-SIM phones and internet calls such as WhatsApp.
- **Real bank SMS.** The sample messages were written for this project.
- **Real users.** Nobody outside the project has used the app yet.
- **Installing from a file** on a phone with Google Play Protect, which may block apps that read SMS.

## One known small issue

When a call ends, the payment guard can log a harmless warning as it shuts down. It does not affect behaviour. A fix is written but not yet built and tested.
