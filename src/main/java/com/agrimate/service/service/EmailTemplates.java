package com.agrimate.service.service;

public final class EmailTemplates {

    private EmailTemplates() {}

    private static final String PRIMARY_DEEP = "#27500A";
    private static final String PALE = "#EAF3DE";
    private static final String SURFACE_ALT = "#F6F8F2";
    private static final String INK = "#1A2E1A";
    private static final String INK_SOFT = "#5A6B57";
    private static final String INK_FAINT = "#8A968A";
    private static final String BORDER = "#E2E8DC";

    public static String otpEmail(String heading, String introText, String code, int ttlMinutes) {
        String content = """
                <h1 style="margin:0 0 12px;font-size:20px;font-weight:800;color:%s;">%s</h1>
                <p style="margin:0 0 24px;font-size:15px;line-height:1.6;color:%s;">%s</p>
                <div style="margin:0 0 20px;padding:18px 16px;background:%s;border:1px solid %s;border-radius:14px;text-align:center;">
                  <span style="font-family:'SFMono-Regular',Consolas,Menlo,monospace;font-size:32px;font-weight:800;letter-spacing:10px;color:%s;">%s</span>
                </div>
                <p style="margin:0;font-size:13px;color:%s;text-align:center;">This code expires in %d minutes.</p>
                <p style="margin:24px 0 0;font-size:13px;color:%s;text-align:center;">If you didn't request this, you can safely ignore this email.</p>
                """.formatted(INK, heading, INK_SOFT, introText, PALE, BORDER, PRIMARY_DEEP, escape(code), INK_FAINT, ttlMinutes, INK_FAINT);
        return shell(content);
    }

    public static String adminInviteEmail(String username, String password) {
        String content = """
                <h1 style="margin:0 0 12px;font-size:20px;font-weight:800;color:%s;">You've been added as an AgriMate admin</h1>
                <p style="margin:0 0 24px;font-size:15px;line-height:1.6;color:%s;">An administrator created an account for you. Use the credentials below to sign in to the admin dashboard, then change your password from Account settings.</p>
                <div style="margin:0 0 20px;padding:18px 16px;background:%s;border:1px solid %s;border-radius:14px;">
                  <div style="font-size:13px;color:%s;margin-bottom:4px;">Username</div>
                  <div style="font-family:'SFMono-Regular',Consolas,Menlo,monospace;font-size:18px;font-weight:800;color:%s;margin-bottom:14px;">%s</div>
                  <div style="font-size:13px;color:%s;margin-bottom:4px;">Temporary password</div>
                  <div style="font-family:'SFMono-Regular',Consolas,Menlo,monospace;font-size:24px;font-weight:800;letter-spacing:4px;color:%s;">%s</div>
                </div>
                <p style="margin:0;font-size:13px;color:%s;text-align:center;">For your security, sign in and change this password as soon as possible.</p>
                """.formatted(INK, INK_SOFT, PALE, BORDER, INK_FAINT, PRIMARY_DEEP, escape(username), INK_FAINT, PRIMARY_DEEP, escape(password), INK_FAINT);
        return shell(content);
    }

    public static String welcomeEmail(String name) {
        String content = """
                <h1 style="margin:0 0 12px;font-size:20px;font-weight:800;color:%s;">Welcome, %s! 🌱</h1>
                <p style="margin:0 0 24px;font-size:15px;line-height:1.6;color:%s;">Your AgriMate account is ready. Here's what you can do next:</p>
                %s
                <p style="margin:24px 0 0;font-size:15px;line-height:1.6;color:%s;">Happy farming!<br/>— The AgriMate Team</p>
                """.formatted(INK, escape(name), INK_SOFT, featureList(), INK_SOFT);
        return shell(content);
    }

    private static String featureList() {
        return feature("🔍", "Scan paddy leaves", "Get instant disease detection with treatment advice")
                + feature("🌾", "Track your farms", "Log farms, crops and harvest history in one place")
                + feature("🩺", "Ask an agronomist", "Get answers from admin-approved experts");
    }

    private static String feature(String emoji, String title, String desc) {
        return """
                <div style="display:flex;align-items:flex-start;margin:0 0 16px;">
                  <div style="flex:0 0 auto;width:40px;height:40px;border-radius:12px;background:%s;text-align:center;line-height:40px;font-size:18px;margin-right:14px;">%s</div>
                  <div>
                    <div style="font-size:15px;font-weight:700;color:%s;">%s</div>
                    <div style="font-size:13px;color:%s;margin-top:2px;">%s</div>
                  </div>
                </div>
                """.formatted(PALE, emoji, INK, title, INK_SOFT, desc);
    }

    private static String shell(String innerHtml) {
        return """
                <!doctype html>
                <html>
                  <body style="margin:0;padding:0;background:%s;font-family:-apple-system,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:%s;padding:32px 16px;">
                      <tr>
                        <td align="center">
                          <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:480px;background:#FFFFFF;border:1px solid %s;border-radius:20px;overflow:hidden;">
                            <tr>
                              <td style="padding:32px 32px 20px;text-align:center;">
                                <div style="width:56px;height:56px;margin:0 auto 12px;border-radius:16px;background:%s;line-height:56px;font-size:26px;">🌾</div>
                                <div style="font-size:18px;font-weight:800;color:%s;letter-spacing:0.3px;">AgriMate</div>
                              </td>
                            </tr>
                            <tr><td style="border-top:1px solid %s;"></td></tr>
                            <tr>
                              <td style="padding:28px 32px 8px;">
                                %s
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:24px 32px;background:%s;border-top:1px solid %s;text-align:center;">
                                <div style="font-size:12px;color:%s;">Healthy paddy, better harvest</div>
                                <div style="font-size:11px;color:%s;margin-top:4px;">This is an automated message — please don't reply.</div>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(SURFACE_ALT, SURFACE_ALT, BORDER, PALE, PRIMARY_DEEP, BORDER, innerHtml, SURFACE_ALT, BORDER, INK_FAINT, INK_FAINT);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
