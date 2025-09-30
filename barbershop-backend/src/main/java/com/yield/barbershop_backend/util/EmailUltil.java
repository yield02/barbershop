package com.yield.barbershop_backend.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class EmailUltil {


    @Value("${app.base-url}") private String appBaseUrl;
    @Value("${app.shop.name}") private String shopName;
    @Value("${app.shop.website-url}") private String shopWebsiteUrl;
    @Value("${app.shop.address}") private String shopAddress;
    @Value("${app.shop.phone}") private String shopPhone;
    @Value("${app.support.phone}") private String supportPhone;

    public static final String CUSTOMER_EMAILVERIFICATION_HTML_TEMPLATE = """
        <!DOCTYPE html>
        <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác minh Tài khoản Nhân viên của bạn</title>
                <style>
                    /* Các style cơ bản để email dễ đọc hơn trên mọi client */
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333333;
                        margin: 0;
                        padding: 20px;
                        background-color: #f4f4f4; /* Nền nhẹ */
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        padding: 25px;
                        border-radius: 8px;
                        box-shadow: 0 0 10px rgba(0,0,0,0.1); /* Bóng nhẹ */
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 25px;
                        background-color: #007bff; /* Màu xanh dương */
                        color: #ffffff !important; /* Quan trọng để đảm bảo màu trắng */
                        text-decoration: none;
                        border-radius: 5px;
                        font-weight: bold;
                        font-size: 16px;
                    }
                    .text-center {
                        text-align: center;
                    }
                    ul {
                        list-style-type: disc;
                        margin-left: 20px;
                        padding: 0;
                    }
                    ul li {
                        margin-bottom: 5px;
                    }
                    strong {
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="text-center">
                        <h2>Barbershop & Quán rượu {{shopName}}</h2>
                        <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                    </div>

                    <p><strong>Kính gửi {{customerName}},</strong></p>

                    <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>Barbershop & Quán rượu {{shopName}} </strong>! Chúng tôi rất vui mừng chào đón bạn đến với cộng đồng của chúng tôi, nơi bạn có thể trải nghiệm các dịch vụ cắt tóc đẳng cấp và thưởng thức những loại đồ uống tuyệt vời.</p>

                    <p>Để hoàn tất quá trình đăng ký và bắt đầu sử dụng đầy đủ các tính năng của tài khoản, chúng tôi cần bạn <strong>xác minh địa chỉ email của mình</strong>. Việc này giúp chúng tôi bảo mật tài khoản của bạn và đảm bảo bạn nhận được các thông báo quan trọng cũng như ưu đãi độc quyền từ chúng tôi.</p>

                    <p>Vui lòng nhấp vào liên kết dưới đây để xác minh tài khoản của bạn:</p>

                    <div class="text-center" style="margin: 25px 0;">
                        <a href="{{verificationLink}}" class="button"><strong>NHẤP VÀO ĐÂY ĐỂ XÁC MINH TÀI KHOẢN NHÂN VIÊN CỦA BẠN</strong></a>
                    </div>
                    <p class="text-center" style="font-size: 14px;">(Liên kết này sẽ hết hạn sau <strong>{{expiryTime}}</strong>.)</p>

                    <p><strong>Tại sao việc xác minh email lại quan trọng?</strong></p>
                    <ul>
                        <li><strong>Bảo mật tài khoản:</strong> Bảo vệ thông tin cá nhân và lịch sử giao dịch của bạn.</li>
                        <li><strong>Truy cập đầy đủ tính năng:</strong> Đặt lịch hẹn, đặt hàng đồ uống, quản lý ưu đãi và hơn thế nữa.</li>
                        <li><strong>Nhận thông tin cập nhật:</strong> Đảm bảo bạn không bỏ lỡ các chương trình khuyến mãi, sự kiện đặc biệt và tin tức mới nhất từ {{shopName}}.</li>
                    </ul>

                    <p>Nếu bạn gặp bất kỳ vấn đề nào trong quá trình xác minh hoặc có bất kỳ câu hỏi nào, xin đừng ngần ngại liên hệ với quản lý của bạn hoặc bộ phận hỗ trợ kỹ thuật tại <strong>{{supportPhone}}</strong>.</p>

                    <p>Nếu bạn nhận được email này do nhầm lẫn, vui lòng bỏ qua.</p>

                    <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">

                    <p class="text-center" style="font-size: 12px; color: #777777;">Trân trọng,</p>
                    <p class="text-center" style="font-size: 12px; color: #777777;">Đội ngũ quản lý <strong>Barbershop & Quán rượu {{shopName}}</strong><br>
                    <a href="{{websiteUrl}}" style="color: #007bff; text-decoration: none;">{{websiteUrl}}</a><br>
                    {{shopAddress}}<br>
                    {{shopPhone}}</p>
                </div>
            </body>
        </html>
    """;


    public static String STAFF_EMAILVERIFICATION_HTML_TEMPLATE = """
            
        <!DOCTYPE html>
        <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác minh Tài khoản Nhân viên của bạn</title>
                <style>
                    /* Các style cơ bản để email dễ đọc hơn trên mọi client */
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333333;
                        margin: 0;
                        padding: 20px;
                        background-color: #f4f4f4; /* Nền nhẹ */
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        padding: 25px;
                        border-radius: 8px;
                        box-shadow: 0 0 10px rgba(0,0,0,0.1); /* Bóng nhẹ */
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 25px;
                        background-color: #007bff; /* Màu xanh dương */
                        color: #ffffff !important; /* Quan trọng để đảm bảo màu trắng */
                        text-decoration: none;
                        border-radius: 5px;
                        font-weight: bold;
                        font-size: 16px;
                    }
                    .text-center {
                        text-align: center;
                    }
                    ul {
                        list-style-type: disc;
                        margin-left: 20px;
                        padding: 0;
                    }
                    ul li {
                        margin-bottom: 5px;
                    }
                    strong {
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="text-center">
                        <h2>Barbershop & Quán rượu {{shopName}}</h2>
                        <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                    </div>

                    <p><strong>Kính gửi {{staffName}},</strong></p>

                    <p>Chào mừng bạn đến với đội ngũ của <strong>Barbershop & Quán rượu {{shopName}}</strong>! Chúng tôi rất vui mừng có bạn là một phần của team để cùng nhau mang đến trải nghiệm tốt nhất cho khách hàng.</p>

                    <p>Để kích hoạt tài khoản nhân viên của bạn và bắt đầu sử dụng hệ thống quản lý nội bộ của chúng tôi, bạn cần <strong>xác minh địa chỉ email này</strong>. Việc xác minh giúp bảo mật thông tin truy cập của bạn và đảm bảo bạn nhận được các thông báo quan trọng liên quan đến công việc từ hệ thống.</p>

                    <p>Vui lòng nhấp vào liên kết dưới đây để xác minh tài khoản của bạn:</p>

                    <div class="text-center" style="margin: 25px 0;">
                        <a href="{{verificationLink}}" class="button"><strong>NHẤP VÀO ĐÂY ĐỂ XÁC MINH TÀI KHOẢN NHÂN VIÊN CỦA BẠN</strong></a>
                    </div>
                    <p class="text-center" style="font-size: 14px;">(Liên kết này sẽ hết hạn sau <strong>{{expiryTime}}</strong>.)</p>

                    <p><strong>Tại sao việc xác minh email lại quan trọng đối với tài khoản nhân viên?</strong></p>
                    <ul>
                        <li><strong>Bảo mật truy cập:</strong> Đảm bảo chỉ bạn mới có quyền truy cập vào các công cụ và thông tin nội bộ.</li>
                        <li><strong>Kích hoạt tài khoản:</strong> Hoàn tất quá trình thiết lập để bạn có thể bắt đầu công việc.</li>
                        <li><strong>Nhận thông báo quan trọng:</strong> Cập nhật về lịch làm việc, thông tin khách hàng, báo cáo và các thông tin điều hành khác.</li>
                    </ul>

                    <p>Nếu bạn gặp bất kỳ vấn đề nào trong quá trình xác minh hoặc có bất kỳ câu hỏi nào, xin đừng ngần ngại liên hệ với quản lý của bạn hoặc bộ phận hỗ trợ kỹ thuật tại <strong>{{supportPhone}}</strong>.</p>

                    <p>Nếu bạn nhận được email này do nhầm lẫn, vui lòng bỏ qua.</p>

                    <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">

                    <p class="text-center" style="font-size: 12px; color: #777777;">Trân trọng,</p>
                    <p class="text-center" style="font-size: 12px; color: #777777;">Đội ngũ quản lý <strong>Barbershop & Quán rượu {{shopName}}</strong><br>
                    <a href="{{websiteUrl}}" style="color: #007bff; text-decoration: none;">{{websiteUrl}}</a><br>
                    {{shopAddress}}<br>
                    {{shopPhone}}</p>
                </div>
            </body>
        </html>

            """;;



    public String getCustomerVerificationTextHtml(String customerName, String verificationLink, LocalDateTime expiryTime) {
        
        return CUSTOMER_EMAILVERIFICATION_HTML_TEMPLATE
                .replace("{{customerName}}", customerName)
                .replace("{{verificationLink}}", verificationLink)
                .replace("{{expiryTime}}", expiryTime.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")))
                .replace("{{shopName}}", shopName)
                .replace("{{shopPhone}}", shopPhone)
                .replace("{{shopAddress}}", shopAddress)
                .replace("{{websiteUrl}}", shopWebsiteUrl)
                .replace("{{supportPhone}}", supportPhone);

    }

    public String getStaffVerificationTextHtml(String staffName, String verificationLink, LocalDateTime expiryTime) {
        
        return STAFF_EMAILVERIFICATION_HTML_TEMPLATE
                .replace("{{staffName}}", staffName)
                .replace("{{verificationLink}}", verificationLink)
                .replace("{{expiryTime}}", expiryTime.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")))
                .replace("{{shopName}}", shopName)
                .replace("{{shopPhone}}", shopPhone)
                .replace("{{shopAddress}}", shopAddress)
                .replace("{{websiteUrl}}", shopWebsiteUrl)
                .replace("{{supportPhone}}", supportPhone);
    }

    
    public String getAppBaseUrl() {
        return appBaseUrl;
    }

}
