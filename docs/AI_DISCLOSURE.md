# AI disclosure

The competition guidelines require teams to disclose any use of AI tools, how they were used, and an estimate of how much of the final work involved them.

## Tool used

An AI coding assistant.

## How it was used

| Area | AI involvement |
|---|---|
| Problem research | The assistant searched the web for fraud statistics, fraud methods and existing tools, and summarised them |
| Choice of idea | The direction was set by the team lead: real-time protection instead of practice, and the specific fraud situations to cover. The assistant proposed how a phone could detect those moments |
| Code | The first draft of the Python engine, the Android app and the tests was written by the assistant |
| Testing | The assistant built the app and ran the checks on an Android emulator |
| Translations | The Marathi and Hindi text was drafted by the assistant |
| Documents and presentation | First drafts were written by the assistant |

## What the team lead did

- Chose the problem area and rejected the first two ideas in favour of real-time protection.
- Named the fraud situations to cover from real experience: OTP calls, fake offer links, fake government messages, and app files sent as documents.
- Set the product rules: never block the person, stay silent for delivery codes and family calls, and work in Marathi, Hindi and English.
- Raised the cases the first design got wrong, such as delivery callers being flagged and a family member asking for a payment.
- Published the repository and prepared the submission.

## AI inside the product

None. Thamba makes its decisions with fixed rules written in Python. It does not call any AI model, so its behaviour is predictable and it works without internet.

## Estimate

Estimated share of the final work that involved AI assistance or generation: **85%**
