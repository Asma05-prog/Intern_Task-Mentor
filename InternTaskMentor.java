import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

public class InternTaskMentor {
    static String internName = "Intern", internEmail = "";
    static String taskTitle = "Build a simple login page";
    static String taskDetails = "Create a responsive login page with email and password fields, validation, and a clear submit button.";
    static String expected = "html,css,validation,responsive,login";
    static String reviewDate = LocalDate.now().plusDays(2).toString();
    static int attempts = 0;
    static String latestFeedback = "No mentor feedback yet.";
    static String emailStatus = "Email is not configured yet.";
    static String latestSubmission = "No submission received yet.";
    static String status = "Waiting for task setup";
    static final List<String> history = new ArrayList<>();
    static final List<MailItem> mailbox = new ArrayList<>();
    static class MailItem {
        String type, from, to, subject, body, date;
        MailItem(String type, String from, String to, String subject, String body) {
            this.type=type; this.from=from; this.to=to; this.subject=subject; this.body=body;
            this.date=java.time.LocalDateTime.now().withNano(0).toString().replace('T',' ');
        }
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8081), 0);
        server.createContext("/", InternTaskMentor::home);
        server.createContext("/assign", InternTaskMentor::assign);
        server.createContext("/submit", InternTaskMentor::submit);
        server.createContext("/feedback", InternTaskMentor::feedback);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Intern Task Mentor running at http://127.0.0.1:8081");
    }

    static void home(HttpExchange ex) throws IOException {
        StringBuilder h = new StringBuilder();
        h.append("""
        <!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Intern Task Mentor</title><style>
        *{box-sizing:border-box}body{margin:0;font-family:Arial,sans-serif;background:#f3f6fb;color:#172033}
        header{background:#182b4d;color:white;padding:24px 7%}header h1{margin:0 0 6px}
        main{max-width:1050px;margin:24px auto;padding:0 16px}.grid{display:grid;grid-template-columns:1fr 1fr;gap:18px}
        .card{background:white;padding:22px;border-radius:14px;box-shadow:0 5px 20px #1c315014}
        .sidebar{position:fixed;left:0;top:0;bottom:0;width:210px;background:#10213e;color:white;padding:24px 16px;z-index:5}
        .sidebar h2{font-size:19px;margin:8px 8px 28px}.sidebar a{display:block;color:#e5edff;text-decoration:none;padding:12px;border-radius:8px;margin:6px 0}.sidebar a:hover{background:#263e65}
        .main-wrap{margin-left:210px}.mail-item{border:1px solid #e1e7f0;border-radius:10px;padding:14px;margin:12px 0;background:#fbfcff}.mail-item h3{margin:0 0 8px}.mail-meta{font-size:13px;color:#637087;margin-bottom:8px}.mail-body{white-space:pre-wrap;overflow-wrap:anywhere}
        label{display:block;font-weight:bold;margin:12px 0 6px}input,textarea{width:100%;padding:11px;border:1px solid #cbd5e1;border-radius:8px;font:inherit}
        textarea{min-height:90px}button{background:#2457d6;color:white;border:0;padding:12px 17px;border-radius:8px;font-weight:bold;cursor:pointer;margin-top:14px}
        .muted{color:#637087;font-size:14px}.pill{display:inline-block;background:#e5edff;padding:6px 10px;border-radius:99px;font-size:13px}
        .lang{border-top:1px solid #e7ebf2;padding-top:10px;margin-top:10px}.link{overflow-wrap:anywhere;color:#174bb5}
        @media(max-width:760px){.grid{grid-template-columns:1fr}header{padding:20px}.sidebar{position:relative;width:auto;bottom:auto;padding:10px;display:flex;gap:6px;align-items:center;flex-wrap:wrap}.sidebar h2{margin:6px;font-size:16px}.sidebar a{padding:9px}.main-wrap{margin-left:0}}
        </style></head><body><aside class="sidebar"><h2>Intern Mentor</h2><a href="/">🏠 Dashboard</a><a href="/#mailbox">📥 Mailbox</a><a href="/submit">📝 Submissions</a></aside><div class="main-wrap"><header><h1>Intern Task Mentor</h1><div>Assign • Explain • Review • Improve</div></header><main><div class="grid">
        <section class="card"><h2>1. Assign a task</h2><form method="post" action="/assign">
        <label>Intern name</label><input name="name" required placeholder="Enter intern name">
        <label>Intern email</label><input name="email" type="email" placeholder="intern@example.com">
        <label>Task title</label><input name="title" required value="Build a simple login page">
        <label>Task instructions</label><textarea name="details" required>Create a responsive login page with email and password fields, validation, and a clear submit button.</textarea>
        <label>Expected-output keywords (comma-separated)</label><input name="expected" value="HTML, CSS, validation, responsive, login">
        <label>Review date</label><input name="date" type="date" required>
        <button type="submit">Assign task</button></form></section>
        """);
        h.append("<section class=\"card\"><h2>2. Task status</h2><span class=\"pill\">").append(esc(status)).append("</span>");
        h.append("<p><b>Intern:</b> ").append(esc(internName)).append("</p><p><b>Task:</b> ").append(esc(taskTitle)).append("</p>");
        h.append("<p><b>Review date:</b> ").append(esc(reviewDate)).append("</p>");
        h.append("<p><b>Submission link:</b> <a class=\"link\" href=\"/submit\">http://127.0.0.1:8081/submit</a></p>");
        h.append("<h3>Task explanation</h3><div class=\"lang\"><b>English</b><p>").append(esc(taskDetails)).append("</p></div>");
        h.append("<div class=\"lang\"><b>Tamil</b><p>Indha task-ai instructions-padi complete panni review date-kulla submit pannunga.</p></div>");
        h.append("<div class=\"lang\"><b>Hindi</b><p>दिए गए निर्देशों के अनुसार काम पूरा करें और समीक्षा की तारीख तक जमा करें।</p></div>");
        h.append("""
        <label>Listen to explanation</label><select id="language"><option value="en-US">English</option><option value="ta-IN">Tamil</option><option value="hi-IN">Hindi</option></select>
        <button onclick="speak()">🔊 Play explanation</button><p class="muted">Browser voice availability depends on installed voices.</p>
        <h3>Latest feedback</h3><p>
        """).append(esc(latestFeedback)).append("</p><p><b>Iteration:</b> ").append(attempts).append(" &nbsp; <b>Status:</b> ").append(esc(status)).append("</p>");
        h.append("<h3>Email status</h3><p>").append(esc(emailStatus)).append("</p>");
        h.append("<h3>Latest submission</h3><p>").append(esc(latestSubmission)).append("</p>");
        h.append("<h3>Write mentor feedback</h3><form method=\"post\" action=\"/feedback\"><label>Feedback for intern</label><textarea name=\"feedback\" required placeholder=\"Type your own feedback here...\"></textarea><button type=\"submit\">Save and email feedback</button></form>");
        h.append("</section></div>");
        h.append("<section class=\"card\" style=\"margin-top:18px\"><h2>3. Submit your work</h2><p>Paste your work or a summary and get demo feedback against expected keywords.</p><a href=\"/submit\"><button>Open submission page</button></a><h3>Review history</h3><ul>").append(historyHtml()).append("</ul>");
        h.append("<p class=\"muted\">Prototype notice: submissions and mailbox messages are stored temporarily in memory and reset when the server restarts. Feedback is written manually by the mentor. External email sending requires SMTP configuration. This local demo is visible only on this computer.</p></section>");
        h.append("<section class=\"card\" id=\"mailbox\" style=\"margin-top:18px\"><h2>📥 Mailbox — Inbox</h2><p class=\"muted\">Task assignments, submission notices, task links, and mentor feedback appear here as temporary in-app messages.</p>").append(mailboxHtml()).append("</section></main></div>");
        h.append("""
        <script>
        function speak(){let l=document.getElementById('language').value;let t=l==='ta-IN'?'Indha task-ai instructions-padi complete panni review date-kulla submit pannunga.':l==='hi-IN'?'दिए गए निर्देशों के अनुसार काम पूरा करें और समीक्षा की तारीख तक जमा करें।':document.querySelector('.lang p').innerText;let u=new SpeechSynthesisUtterance(t);u.lang=l;speechSynthesis.cancel();speechSynthesis.speak(u);}
        </script></body></html>
        """);
        send(ex, 200, h.toString());
    }

    static void assign(HttpExchange ex) throws IOException {
        Map<String,String> f = form(ex);
        internName = f.getOrDefault("name","Intern");
        internEmail = f.getOrDefault("email","");
        taskTitle = f.getOrDefault("title","Assigned task");
        taskDetails = f.getOrDefault("details","");
        expected = f.getOrDefault("expected","").toLowerCase(Locale.ROOT);
        reviewDate = f.getOrDefault("date",LocalDate.now().plusDays(2).toString());
        attempts = 0; latestFeedback = "Task assigned. Submit your work using the submission page.";
        status = "Task assigned";
        history.clear(); latestSubmission = "No submission received yet.";
        history.add("Task assigned on " + LocalDate.now() + "; review date: " + reviewDate);
        mailbox.add(0, new MailItem("TASK ASSIGNMENT", "Mentor", internEmail.isBlank() ? "Intern (demo inbox)" : internEmail,
            "Task assigned: " + taskTitle, "Hello " + internName + ",\n\nA new task has been assigned.\nTask: " + taskTitle + "\nInstructions: " + taskDetails + "\nReview date: " + reviewDate + "\nSubmission link: http://127.0.0.1:8081/submit"));
        if (internEmail.isBlank()) emailStatus = "Task saved, but no email sent because intern email was not provided.";
        else {
            try { sendEmail(internEmail, "Task assigned: " + taskTitle, "Hello " + internName + ",\n\nA new task has been assigned to you.\n\nTask: " + taskTitle + "\nInstructions: " + taskDetails + "\nReview date: " + reviewDate + "\nSubmission page: http://127.0.0.1:8081/submit\n\nRegards,\nIntern Task Mentor"); emailStatus = "Task assignment email sent to " + internEmail; }
            catch (Exception e) { emailStatus = "Email not sent: " + e.getMessage() + ". Check SMTP environment settings in README.txt."; }
        }
        redirect(ex, "/");
    }

    static void submit(HttpExchange ex) throws IOException {
        if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            Map<String,String> f = form(ex);
            String work = f.getOrDefault("work","");
            attempts++;
            latestSubmission = work;
            status = "Submission received — waiting for mentor review";
            latestFeedback = "No mentor feedback yet. Please review the submission and enter feedback below.";
            history.add("Submission attempt " + attempts + " received on " + LocalDate.now());
            mailbox.add(0, new MailItem("SUBMISSION NOTICE", internName, "Mentor", "Submission received: " + taskTitle,
                "A submission was received on " + LocalDate.now() + ".\n\nSubmitted work:\n" + work));
            redirect(ex, "/"); return;
        }
        String body = """
        <!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Submit work</title>
        <style>body{font-family:Arial;background:#f3f6fb;margin:0;padding:24px;color:#172033}.card{max-width:760px;margin:auto;background:white;padding:24px;border-radius:14px}textarea{width:100%;min-height:260px;padding:12px;font:inherit;border:1px solid #cbd5e1;border-radius:8px}button{background:#2457d6;color:white;padding:12px 18px;border:0;border-radius:8px;margin-top:12px;cursor:pointer}a{color:#2457d6}</style></head><body><div class="card">
        <h1>Submit your task</h1>
        """;
        body += "<p><b>Task:</b> " + esc(taskTitle) + "</p><p><b>Review date:</b> " + esc(reviewDate) + "</p>";
        body += """
        <form method="post" action="/submit"><label for="work">Paste your work, solution summary, or evidence below:</label><br><br><textarea id="work" name="work" required placeholder="Describe your completed work and how it meets each expected criterion..."></textarea><br><button type="submit">Submit for review</button></form>
        <p><a href="/">← Back to dashboard</a></p><p style="color:#637087">This prototype accepts text only. Mentor feedback is entered on the dashboard. Email sending requires SMTP settings in environment variables.</p></div></body></html>
        """;
        send(ex, 200, body);
    }

    static void feedback(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { redirect(ex, "/"); return; }
        Map<String,String> f = form(ex);
        String feedbackText = f.getOrDefault("feedback", "").trim();
        if (feedbackText.isEmpty()) { redirect(ex, "/"); return; }
        latestFeedback = feedbackText;
        status = "Feedback entered by mentor";
        history.add("Mentor feedback saved on " + LocalDate.now() + ": " + feedbackText);
        mailbox.add(0, new MailItem("MENTOR FEEDBACK", "Mentor", internEmail.isBlank() ? "Intern (demo inbox)" : internEmail,
            "Feedback for task: " + taskTitle, "Hello " + internName + ",\n\nYour mentor has provided feedback.\n\n" + feedbackText));
        if (internEmail.isBlank()) emailStatus = "Feedback saved, but email not sent because intern email is missing.";
        else {
            try { sendEmail(internEmail, "Feedback for task: " + taskTitle, "Hello " + internName + ",\n\nYour mentor has provided feedback for the task: " + taskTitle + "\n\nFeedback:\n" + feedbackText + "\n\nRegards,\nIntern Task Mentor"); emailStatus = "Feedback email sent to " + internEmail; }
            catch (Exception e) { emailStatus = "Feedback saved, but email failed: " + e.getMessage() + ". Check SMTP environment settings in README.txt."; }
        }
        redirect(ex, "/");
    }

    // Configure SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_APP_PASSWORD, and SMTP_FROM as environment variables.
    static void sendEmail(String to, String subject, String body) throws Exception {
        String host = System.getenv().getOrDefault("SMTP_HOST", "smtp.gmail.com");
        int port = Integer.parseInt(System.getenv().getOrDefault("SMTP_PORT", "587"));
        String user = System.getenv("SMTP_USER");
        String pass = System.getenv("SMTP_APP_PASSWORD");
        String from = System.getenv().getOrDefault("SMTP_FROM", user == null ? "" : user);
        if (user == null || user.isBlank() || pass == null || pass.isBlank() || from.isBlank())
            throw new IOException("SMTP_USER, SMTP_APP_PASSWORD, and SMTP_FROM are not configured");
        Socket plain = new Socket(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(plain.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(plain.getOutputStream(), StandardCharsets.UTF_8));
        expect(in, 220);
        command(out, in, "EHLO localhost", 250);
        command(out, in, "STARTTLS", 220);
        SSLSocket ssl = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(plain, host, port, true);
        ssl.startHandshake();
        in = new BufferedReader(new InputStreamReader(ssl.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(ssl.getOutputStream(), StandardCharsets.UTF_8));
        command(out, in, "EHLO localhost", 250);
        command(out, in, "AUTH LOGIN", 334);
        command(out, in, Base64.getEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8)), 334);
        command(out, in, Base64.getEncoder().encodeToString(pass.getBytes(StandardCharsets.UTF_8)), 235);
        command(out, in, "MAIL FROM:<" + from + ">", 250);
        command(out, in, "RCPT TO:<" + to + ">", 250, 251);
        command(out, in, "DATA", 354);
        String safeSubject = subject.replace("\r", " ").replace("\n", " ");
        String message = "From: " + from + "\r\nTo: " + to + "\r\nSubject: " + safeSubject + "\r\nMIME-Version: 1.0\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n" + body.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "\r\n").replace("\n.", "\n..").replaceAll("(?m)^\\.", "..");
        out.write(message + "\r\n.\r\n"); out.flush(); expect(in, 250);
        command(out, in, "QUIT", 221);
        ssl.close();
    }
    static void command(BufferedWriter out, BufferedReader in, String command, int expected) throws IOException {
        command(out, in, command, expected, -1);
    }
    static void command(BufferedWriter out, BufferedReader in, String command, int expected, int alternate) throws IOException {
        out.write(command + "\r\n"); out.flush(); int code = readCode(in);
        if (code != expected && code != alternate) throw new IOException("SMTP response " + code + " after command " + command + ": " + lastResponse);
    }
    static String lastResponse = "";
    static void expect(BufferedReader in, int expected) throws IOException {
        int code = readCode(in); if (code != expected) throw new IOException("SMTP expected " + expected + " but received " + code + ": " + lastResponse);
    }
    static int readCode(BufferedReader in) throws IOException {
        String line; int code = -1;
        do { line = in.readLine(); if (line == null) throw new IOException("SMTP connection closed"); lastResponse = line; if (line.length() >= 3) try { code = Integer.parseInt(line.substring(0,3)); } catch (NumberFormatException ignored) {} }
        while (line.length() > 3 && line.charAt(3) == '-');
        return code;
    }

    static Map<String,String> form(HttpExchange ex) throws IOException {
        String raw = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String,String> map = new HashMap<>();
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=",2);
            map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return map;
    }
    static String mailboxHtml() {
        if (mailbox.isEmpty()) return "<p class=\"muted\">Your inbox is empty. Assign a task or save mentor feedback to create a message.</p>";
        StringBuilder b = new StringBuilder();
        for (MailItem m : mailbox) {
            b.append("<article class=\"mail-item\"><h3>").append(esc(m.subject)).append("</h3>")
             .append("<div class=\"mail-meta\"><b>").append(esc(m.type)).append("</b> · From: ").append(esc(m.from))
             .append(" · To: ").append(esc(m.to)).append(" · ").append(esc(m.date)).append("</div>")
             .append("<div class=\"mail-body\">").append(esc(m.body)).append("</div></article>");
        }
        return b.toString();
    }

    static String historyHtml() {
        if (history.isEmpty()) return "<li>No reviews yet.</li>";
        StringBuilder b = new StringBuilder();
        for (int i=history.size()-1;i>=0;i--) b.append("<li>").append(esc(history.get(i))).append("</li>");
        return b.toString();
    }
    static String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");
    }
    static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type","text/html; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os=ex.getResponseBody()) { os.write(bytes); }
    }
    static void redirect(HttpExchange ex, String path) throws IOException {
        ex.getResponseHeaders().set("Location",path);
        ex.sendResponseHeaders(303,-1); ex.close();
    }
}
