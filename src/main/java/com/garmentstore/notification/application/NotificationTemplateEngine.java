package com.garmentstore.notification.application;

import com.garmentstore.notification.domain.NotificationChannelType;
import com.garmentstore.notification.domain.NotificationType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Builds rendered notification messages for each event type and channel.
 *
 * Myntra/Ajio-level: messages are friendly, branded, and informative.
 * WhatsApp messages are concise (max 1600 chars, must be clear on mobile).
 * Email messages can be richer HTML (handled by EmailNotificationChannel wrapper).
 */
@Component
public class NotificationTemplateEngine {

    public record NotificationContent(String subject, String body) {}

    public NotificationContent build(
            NotificationType type,
            NotificationChannelType channel,
            TemplateContext ctx) {

        if (type == null) {
            return new NotificationContent("", "");
        }

        return switch (type.name()) {
            case "ORDER_PLACED"    -> orderPlaced(channel, ctx);
            case "ORDER_CONFIRMED" -> orderConfirmed(channel, ctx);
            case "ORDER_SHIPPED"   -> orderShipped(channel, ctx);
            case "ORDER_DELIVERED" -> orderDelivered(channel, ctx);
            case "ORDER_CANCELLED" -> orderCancelled(channel, ctx);
            case "NEW_ORDER_ADMIN" -> newOrderAdmin(channel, ctx);
            case "LOW_STOCK_ADMIN" -> lowStockAdmin(channel, ctx);
            case "WELCOME"         -> welcome(channel, ctx);
            default                -> new NotificationContent("", "");
        };
    }

    // ── Customer: Order Placed ─────────────────────────────────────────────────

    private NotificationContent orderPlaced(NotificationChannelType ch, TemplateContext ctx) {
        String subject = "Order Confirmed! #" + ctx.orderNumber();
        String body = isWhatsApp(ch)
            ? """
              🛍️ *Vastra* — Order Placed!

              Hi %s! 🎉

              Your order *#%s* has been placed successfully.
              Amount: ₹%s

              We'll notify you once it's confirmed and packed.

              Track your order: https://vastra.in/account/orders
              """.formatted(ctx.customerName(), ctx.orderNumber(), ctx.amount())
            : """
              <h2>🎉 Your order is confirmed!</h2>
              <p>Hi <strong>%s</strong>,</p>
              <p>Thank you for shopping with Vastra! Your order <strong>#%s</strong> has been placed.</p>
              <table style="border-collapse:collapse;width:100%%">
                <tr><td style="padding:8px;color:#888;">Order ID</td><td style="padding:8px;font-weight:600;">#%s</td></tr>
                <tr><td style="padding:8px;color:#888;">Amount</td><td style="padding:8px;font-weight:600;">₹%s</td></tr>
                <tr><td style="padding:8px;color:#888;">Status</td><td style="padding:8px;color:#27ae60;font-weight:600;">✅ Placed</td></tr>
              </table>
              <p>We'll send you another update when your order is confirmed and packed.</p>
              <a class="cta" href="https://vastra.in/account/orders">Track Your Order →</a>
              """.formatted(ctx.customerName(), ctx.orderNumber(), ctx.orderNumber(), ctx.amount());
        return new NotificationContent(subject, body);
    }

    // ── Customer: Order Confirmed ──────────────────────────────────────────────

    private NotificationContent orderConfirmed(NotificationChannelType ch, TemplateContext ctx) {
        String subject = "Your Vastra Order is Being Prepared! #" + ctx.orderNumber();
        String body = isWhatsApp(ch)
            ? """
              ✅ *Vastra* — Order Confirmed!

              Hi %s!

              Great news! Your order *#%s* has been confirmed and is being prepared for dispatch.

              We'll notify you with tracking details once it ships! 🚚
              """.formatted(ctx.customerName(), ctx.orderNumber())
            : """
              <h2>✅ Order Confirmed!</h2>
              <p>Hi <strong>%s</strong>,</p>
              <p>Your order <strong>#%s</strong> has been confirmed by our team and is now being carefully packed.</p>
              <p>We'll send you tracking details the moment it ships!</p>
              <a class="cta" href="https://vastra.in/account/orders">View Order →</a>
              """.formatted(ctx.customerName(), ctx.orderNumber());
        return new NotificationContent(subject, body);
    }

    // ── Customer: Order Shipped ────────────────────────────────────────────────

    private NotificationContent orderShipped(NotificationChannelType ch, TemplateContext ctx) {
        String subject = "Your Vastra Order is On Its Way! 🚚 #" + ctx.orderNumber();
        String body = isWhatsApp(ch)
            ? """
              🚚 *Vastra* — Your order has shipped!

              Hi %s!

              Your order *#%s* is on its way! 🎁

              It will be delivered to your address within 2-5 business days.

              Track your order: https://vastra.in/account/orders
              """.formatted(ctx.customerName(), ctx.orderNumber())
            : """
              <h2>🚚 Your Order is On Its Way!</h2>
              <p>Hi <strong>%s</strong>,</p>
              <p>Exciting news! Your order <strong>#%s</strong> has been shipped and is heading your way.</p>
              <p><strong>Expected delivery:</strong> 2-5 business days</p>
              <p>Sit tight — your Vastra fashion is coming! 🎁</p>
              <a class="cta" href="https://vastra.in/account/orders">Track Order →</a>
              """.formatted(ctx.customerName(), ctx.orderNumber());
        return new NotificationContent(subject, body);
    }

    // ── Customer: Order Delivered ──────────────────────────────────────────────

    private NotificationContent orderDelivered(NotificationChannelType ch, TemplateContext ctx) {
        String subject = "Your Vastra Order Has Arrived! 📦 #" + ctx.orderNumber();
        String body = isWhatsApp(ch)
            ? """
              📦 *Vastra* — Order Delivered!

              Hi %s! 🎉

              Your order *#%s* has been delivered successfully.

              We hope you love your new look! Style it, flaunt it! ✨

              Share your experience: https://vastra.in/reviews
              """.formatted(ctx.customerName(), ctx.orderNumber())
            : """
              <h2>📦 Your Order Has Arrived!</h2>
              <p>Hi <strong>%s</strong>,</p>
              <p>Your order <strong>#%s</strong> has been delivered! We hope you love your new pieces.</p>
              <p>Style them, flaunt them — and don't forget to share your looks! ✨</p>
              <a class="cta" href="https://vastra.in/account/orders">Rate Your Order →</a>
              """.formatted(ctx.customerName(), ctx.orderNumber());
        return new NotificationContent(subject, body);
    }

    // ── Customer: Order Cancelled ──────────────────────────────────────────────

    private NotificationContent orderCancelled(NotificationChannelType ch, TemplateContext ctx) {
        String subject = "Order Cancelled — #" + ctx.orderNumber();
        String body = isWhatsApp(ch)
            ? """
              ❌ *Vastra* — Order Cancelled

              Hi %s,

              Your order *#%s* has been cancelled.

              If you have any questions, please contact our support.

              Shop again: https://vastra.in
              """.formatted(ctx.customerName(), ctx.orderNumber())
            : """
              <h2>❌ Order Cancelled</h2>
              <p>Hi <strong>%s</strong>,</p>
              <p>Your order <strong>#%s</strong> has been cancelled.</p>
              <p>If you believe this is an error or need help, please contact our support team.</p>
              <a class="cta" href="https://vastra.in/products">Continue Shopping →</a>
              """.formatted(ctx.customerName(), ctx.orderNumber());
        return new NotificationContent(subject, body);
    }

    // ── Admin: New Order Received ──────────────────────────────────────────────

    private NotificationContent newOrderAdmin(NotificationChannelType ch, TemplateContext ctx) {
        String subject = "🛒 New Order Received — #" + ctx.orderNumber();
        String body = isWhatsApp(ch)
            ? """
              🛒 *Vastra Admin* — New Order!

              Order *#%s* received from *%s*.
              Amount: ₹%s

              Action required: Please confirm the order.
              Dashboard: https://vastra.in/admin/orders
              """.formatted(ctx.orderNumber(), ctx.customerName(), ctx.amount())
            : """
              <h2>🛒 New Order Received!</h2>
              <p>A new order has been placed on Vastra.</p>
              <table style="border-collapse:collapse;width:100%%">
                <tr><td style="padding:8px;color:#888;">Order</td><td style="padding:8px;font-weight:600;">#%s</td></tr>
                <tr><td style="padding:8px;color:#888;">Customer</td><td style="padding:8px;">%s</td></tr>
                <tr><td style="padding:8px;color:#888;">Amount</td><td style="padding:8px;font-weight:600;">₹%s</td></tr>
                <tr><td style="padding:8px;color:#888;">Status</td><td style="padding:8px;color:#f39c12;font-weight:600;">⏳ Pending</td></tr>
              </table>
              <a class="cta" href="https://vastra.in/admin/orders">Manage Order →</a>
              """.formatted(ctx.orderNumber(), ctx.customerName(), ctx.amount());
        return new NotificationContent(subject, body);
    }

    // ── Admin: Low Stock Alert ─────────────────────────────────────────────────

    private NotificationContent lowStockAdmin(NotificationChannelType ch, TemplateContext ctx) {
        String subject = "⚠️ Low Stock Alert — " + ctx.productName();
        String body = isWhatsApp(ch)
            ? """
              ⚠️ *Vastra Admin* — Low Stock Alert!

              Product: *%s*
              SKU: %s
              Remaining: *%s units*

              Please restock immediately to avoid missed sales.
              Dashboard: https://vastra.in/admin/inventory
              """.formatted(ctx.productName(), ctx.sku(), ctx.stockRemaining())
            : """
              <h2>⚠️ Low Stock Alert</h2>
              <p>A product is running critically low on stock.</p>
              <table style="border-collapse:collapse;width:100%%">
                <tr><td style="padding:8px;color:#888;">Product</td><td style="padding:8px;font-weight:600;">%s</td></tr>
                <tr><td style="padding:8px;color:#888;">SKU</td><td style="padding:8px;">%s</td></tr>
                <tr><td style="padding:8px;color:#888;">Stock Left</td><td style="padding:8px;color:#e74c3c;font-weight:600;">%s units</td></tr>
              </table>
              <a class="cta" href="https://vastra.in/admin/inventory">Update Inventory →</a>
              """.formatted(ctx.productName(), ctx.sku(), ctx.stockRemaining());
        return new NotificationContent(subject, body);
    }

    // ── Customer: Welcome ──────────────────────────────────────────────────────

    private NotificationContent welcome(NotificationChannelType ch, TemplateContext ctx) {
        String subject = "Welcome to Vastra — India's Fashion Destination 🎉";
        String body = isWhatsApp(ch)
            ? """
              👗 *Welcome to Vastra!*

              Hi %s! 🎉

              Your account has been created successfully.

              Explore thousands of styles for Men, Women & Kids.
              Shop now: https://vastra.in
              """.formatted(ctx.customerName())
            : """
              <h2>Welcome to Vastra, %s! 🎉</h2>
              <p>We're thrilled to have you join India's growing fashion destination.</p>
              <p>Explore thousands of curated styles for Men, Women & Kids — from everyday casuals to festive specials.</p>
              <ul>
                <li>✅ Free delivery on orders above ₹499</li>
                <li>✅ Easy 30-day returns</li>
                <li>✅ Exclusive member deals</li>
              </ul>
              <a class="cta" href="https://vastra.in/products">Start Shopping →</a>
              """.formatted(ctx.customerName());
        return new NotificationContent(subject, body);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private boolean isWhatsApp(NotificationChannelType ch) {
        return ch == NotificationChannelType.WHATSAPP;
    }

    // ── Context record — carries all data needed to build any template ─────────

    public record TemplateContext(
            String customerName,
            String orderNumber,
            BigDecimal amount,
            String productName,
            String sku,
            Integer stockRemaining
    ) {
        /** Convenience factory for order-related events. */
        public static TemplateContext forOrder(String customerName, String orderNumber, BigDecimal amount) {
            return new TemplateContext(customerName, orderNumber, amount, null, null, null);
        }

        /** Convenience factory for stock alert events. */
        public static TemplateContext forStock(String productName, String sku, Integer stockRemaining) {
            return new TemplateContext(null, null, null, productName, sku, stockRemaining);
        }

        /** Convenience factory for welcome event. */
        public static TemplateContext forUser(String customerName) {
            return new TemplateContext(customerName, null, null, null, null, null);
        }
    }
}
