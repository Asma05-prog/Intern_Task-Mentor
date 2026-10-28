INTERN TASK MENTOR - UPDATED JAVA VERSION

Features
- Assign a task to an intern and set a review date.
- Send task assignment email using Gmail SMTP (after configuring an app password).
- Accept a text submission from the submission page.
- Let the mentor type their own feedback on the dashboard.
- Save the manually written feedback in the review history and email it to the intern.
- English/Tamil/Hindi explanation text and browser speech playback.
- Sidebar navigation with a temporary in-app Mailbox showing task assignments, submission notices, links, and mentor feedback.

Requirements
- JDK 17 or newer recommended.
- Internet connection to send email.
- Gmail account with 2-Step Verification and a Google App Password for SMTP.

Run on Windows
1. Open this folder in File Explorer.
2. Click the address bar, type cmd, and press Enter.
3. Configure SMTP variables in that Command Prompt window (replace the sample values):

   set SMTP_HOST=smtp.gmail.com
   set SMTP_PORT=587
   set SMTP_USER=yourgmail@gmail.com
   set SMTP_FROM=yourgmail@gmail.com
   set SMTP_APP_PASSWORD=your_16_character_google_app_password

4. Compile and run:

   javac InternTaskMentor.java
   java InternTaskMentor

5. Open this in your browser:

   http://127.0.0.1:8081

Email setup notes
- SMTP_USER and SMTP_FROM should normally be the Gmail address used to create the app password.
- Use a Google App Password, not your normal Gmail password. App Passwords require 2-Step Verification and may be unavailable for some managed/work accounts.
- Never upload the app password to GitHub or send it to anyone. Do not put it directly into the Java source code.
- If SMTP settings are missing or email fails, the task/feedback is still saved locally and the dashboard shows an email status/error.
- Email is attempted when a task is assigned (if an intern email is provided) and when the mentor saves feedback.

Important limitations
- This is a local prototype. It listens on 127.0.0.1 and is only accessible on the computer running it.
- The submission URL shown in email is a local URL, so a remote intern cannot open it from another computer unless the app is deployed to a public server and its URL is changed.
- Task state, review history, and the in-app Mailbox are stored temporarily in memory and reset when the Java server restarts. The Mailbox is an internal demo inbox, not a real email provider inbox.
- Submissions accept text only; no file attachments or database persistence are implemented.
- Feedback is written by the mentor; the keyword-based automatic feedback has been removed.
