# Fraud coverage and limits

Thamba cannot hear a phone call and cannot read chats. It reacts to events on the phone. That decides what it can and cannot help with.

The list of fraud types below was compiled by the author from news reports and police advisories. It is not an official classification. The coverage ratings describe what the rules are designed to catch; they are the author's judgement, not measured results.

## Coverage

| Fraud | How the money is lost | Guard | Coverage |
|---|---|---|---|
| Caller asks for an OTP or PIN | A fake bank or wallet "officer" needs the code to finish a payment | Code | Strong |
| Fake customer-care number | The person searches for a helpline, reaches a fraudster, and is asked for a code | Code | Strong |
| Free gift and offer links | A link opens a fake page that asks for card details or UPI PIN | Link | Strong for obvious bait; a plain-looking new address may pass |
| Fake government messages | Challan, electricity or KYC message with a look-alike link | Message, Link | Strong when sent from a mobile number or using a protected name |
| App file disguised as a document | A "challan" or "invitation" file is really an app that reads OTPs | Link catches the download link; App finds it when the person runs a check | Partial: there is no warning at the moment of install |
| Screen-sharing "help" | The caller has the person install a remote-access app | App lists it; Payment asks before money is sent | Partial |
| "Digital arrest" | Fake police keep the person on a call and make them transfer money themselves | Payment | Partial: ordinary phone calls only, and only the listed payment apps |
| QR code and "receive money" tricks | A QR that takes money instead of giving it | Payment | Partial, when the buyer is on a phone call |
| Voice of a "relative" in trouble | An imitated voice asks for urgent money from an unknown number | Payment | Partial |
| Loan app harassment | A loan app copies contacts and photos and blackmails | App, only if it also reads SMS and was not from a store | Weak |
| Fake investment and trading groups | Weeks of fake profits, then large transfers made willingly | Link | Weak |
| Task and job scams | Small payouts first, then a "deposit" to continue | Link | Weak |
| Caller asks for documents | A fake loan or bank "officer" asks for Aadhaar, PAN or bank papers | Link, only if a link is used | Weak |
| Romance and sextortion | Trust or blackmail over chat and video | None | Not covered |
| SIM swap, Aadhaar fingerprint withdrawal, call-forwarding codes | Happens at a shop, a bank agent or the network | None | Not covered |

## Known limits

- **It cannot hear calls.** A fraud that involves only talking, followed by something done away from the phone, is invisible to it.
- **Android only.** iPhones do not allow an app to see incoming SMS or call state in this way.
- **WhatsApp and other internet calls.** The app sees ordinary phone calls. It does not know who is on an internet call, so the code and payment guards do not act during one.
- **Outgoing calls.** On some phones the number of an outgoing call may not be available. The caller is then treated as unknown, which can cause an unnecessary warning.
- **Wrong warnings.** If a helper who is not a saved contact is on the call when a code arrives or a payment app opens, a warning appears. The person can close it and continue.
- **Payment guard needs extra access.** The person must allow Thamba to see which app is on screen. It knows only the payment apps in its list, and it asks once per call.
- **App guard is a check, not a watch.** It runs when the person asks. It does not yet detect apps that take over the screen through the accessibility service.
- **Message wording varies.** Banks word their SMS differently. The rules were tested on a small set of sample messages written for this project, not on a large set of real ones.
- **Link rules are simple.** They catch look-alikes of a short list of protected names, bait words and hidden addresses. A fraud site with a plain new address is not caught.
- **It cannot recover money.** The complaint helper only shortens the time to report.
- **Installing the app.** It needs SMS and call permissions. Some phones, and Google Play Protect in India, may block installing such an app from outside the Play Store.

## What would make it stronger

- A large set of real, anonymised SMS from many banks to test the code guard.
- A trial with real users, measuring how often warnings are correct and whether people act on them.
- Checking the app and payment guards on phones from several manufacturers, since these features behave differently across them.
