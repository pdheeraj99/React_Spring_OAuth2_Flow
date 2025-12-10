package com.oauth2.resource_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
public class PhotosController {

    @GetMapping("/photos")
    public String getPhotos(@AuthenticationPrincipal Jwt jwt) {
        // Extract user info from JWT
        String userId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");

        System.out.println("🔐 Resource Server: Fetching photos for User ID: " + userId);

        // Build HTML response with actual photos
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial; padding: 20px; max-width: 800px;'>");

        // Title
        html.append("<h1 style='color: #6366f1;'>📸 Your Personal Photo Gallery</h1>");

        // Success Box
        html.append(
                "<div style='background: #e8f5e9; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #4caf50;'>");
        html.append("<h3 style='color: #2e7d32; margin: 0;'>✅ JWT Successfully Validated!</h3>");
        html.append(
                "<p style='margin: 10px 0 0 0;'>Resource Server verified your identity using Google's public keys.</p>");
        html.append("</div>");

        // User Identity Table
        html.append("<h2>👤 Your Identity (from JWT):</h2>");
        html.append("<table style='border-collapse: collapse; width: 100%; margin: 15px 0;'>");
        html.append(
                "<tr style='background: #f5f5f5;'><td style='padding: 10px; border: 1px solid #ddd;'><strong>User ID</strong></td>");
        html.append("<td style='padding: 10px; border: 1px solid #ddd;'><code>").append(userId)
                .append("</code></td></tr>");
        html.append("<tr><td style='padding: 10px; border: 1px solid #ddd;'><strong>Email</strong></td>");
        html.append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(email != null ? email : "N/A")
                .append("</td></tr>");
        html.append(
                "<tr style='background: #f5f5f5;'><td style='padding: 10px; border: 1px solid #ddd;'><strong>Name</strong></td>");
        html.append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(name != null ? name : "N/A")
                .append("</td></tr>");
        html.append("</table>");

        // SQL Query Example
        html.append(
                "<div style='background: #fff3e0; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #ff9800;'>");
        html.append("<h3 style='color: #e65100; margin: 0;'>💡 How User ID is Used in Real Apps:</h3>");
        html.append("<p style='margin: 10px 0;'><code>SELECT * FROM photos WHERE user_id = '").append(userId)
                .append("'</code></p>");
        html.append("<p style='margin: 0;'>This User ID uniquely identifies YOU. Only YOUR photos are fetched!</p>");
        html.append("</div>");

        // Actual Photos Grid - Using User ID as seed so each user sees different
        // photos!
        String shortId = userId.substring(0, Math.min(6, userId.length()));
        html.append("<h2>🖼️ Your Photos (Personalized for User ").append(shortId).append("...):</h2>");
        html.append("<div style='display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin: 20px 0;'>");

        // Photo 1
        html.append("<div style='text-align: center;'>");
        html.append("<img src='https://picsum.photos/seed/").append(userId)
                .append("a/200/200' style='border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);' />");
        html.append("<p style='margin: 8px 0 0; color: #666;'>Beach Vacation 🏖️</p></div>");

        // Photo 2
        html.append("<div style='text-align: center;'>");
        html.append("<img src='https://picsum.photos/seed/").append(userId)
                .append("b/200/200' style='border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);' />");
        html.append("<p style='margin: 8px 0 0; color: #666;'>Mountain Trip 🏔️</p></div>");

        // Photo 3
        html.append("<div style='text-align: center;'>");
        html.append("<img src='https://picsum.photos/seed/").append(userId)
                .append("c/200/200' style='border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);' />");
        html.append("<p style='margin: 8px 0 0; color: #666;'>City Lights 🌃</p></div>");

        html.append("</div>");

        // Security Note
        html.append(
                "<div style='background: #e3f2fd; padding: 15px; border-radius: 8px; margin-top: 20px; border-left: 4px solid #2196f3;'>");
        html.append("<h3 style='color: #1565c0; margin: 0;'>🔒 Security Note:</h3>");
        html.append(
                "<p style='margin: 10px 0 0 0;'>These photos are <strong>ONLY visible to you</strong> because:<br>");
        html.append("1. Your JWT was validated ✓<br>");
        html.append("2. Your User ID was extracted ✓<br>");
        html.append("3. Photos are filtered by YOUR User ID ✓</p></div>");

        html.append("</div>");

        return html.toString();
    }
}
