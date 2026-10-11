# Privacy

Thamba needs sensitive permissions to do its job. This page says what each one is used for and what the app does not do.

## What stays on the phone

Everything. The app has no server and makes no network requests. The Android manifest does not request the internet permission, so the app cannot send data out even by mistake.

## What it reads, and why

| Permission | Used for | Stored? |
|---|---|---|
| Receive SMS | Checking an incoming message for a one-time code, a risky link or a fake sender, at the moment it arrives | No. The text is checked in memory and discarded |
| Phone state, call log, outgoing calls | Knowing whether a call is on and which number it is with | Only the current call's number, erased when the call ends |
| Contacts | Checking whether that one number is saved. The contact list is not read out or copied | No |
| Usage access | Payment guard: during a call only, seeing the name of the app on screen to notice a payment app. It does not see what is inside the app | No |
| List of installed apps | App guard: when the person asks for a check, looking at each app's name, where it was installed from and which permissions it asks for | No |
| Remove apps | Opening Android's own "uninstall?" question for an app the person chooses to remove | No |
| Answer phone calls | The "Cut the call" button on the warning | No |
| Display over other apps, full-screen notifications | Showing the warning on top of the call screen | No |
| Foreground service | Keeping the payment guard running for the length of a call. A notification shows while it runs | No |
| Notifications, vibration | The small notes and the alert | No |

## What it keeps

Two settings: the chosen language and one family phone number entered during setup. They are stored in the app's private storage and removed when the app is uninstalled.

## What it never does

- It does not read old messages in the inbox.
- It does not record or listen to calls.
- It does not read WhatsApp or any other chat.
- It does not see amounts, recipients or anything else inside a payment app.
- It does not watch which apps are used outside a phone call.
- It does not collect names, account numbers or codes.
- The "Money gone?" helper does not save what is typed into it.

## Honest cautions

- This app asks for more access than most apps should have. That is the cost of acting at the moment of danger, and the reason the code is open and the app has no internet permission.
- Every guard works independently. A person who is not comfortable with one permission can leave it off and keep the others.
- Link checking is done with rules on the phone. If an online safe-browsing check is added later, tapped addresses would be sent to that service, and this page and the app's setup screen would have to say so first.
