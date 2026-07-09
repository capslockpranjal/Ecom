package org.example.zenvybackend.common.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageResolver {

    private static final Map<String, String> EXCEPTION_MESSAGE_CODES = Map.ofEntries(
            Map.entry("No customers registered", "error.admin.customers.none"),
            Map.entry("No sellers registered", "error.admin.sellers.none"),
            Map.entry("Customer account already active", "error.customer.already_active"),
            Map.entry("Customer account already deactivated", "error.customer.already_deactivated"),
            Map.entry("User is not a seller", "error.seller.invalid_user"),
            Map.entry("Seller account already active", "error.seller.already_active"),
            Map.entry("Seller account already deactivated", "error.seller.already_deactivated"),
            Map.entry("Product not found", "error.product.not_found"),
            Map.entry("Product is already active", "error.product.already_active"),
            Map.entry("Product is already inactive", "error.product.already_inactive"),
            Map.entry("Category must be a leaf category", "error.category.must_be_leaf"),
            Map.entry("Product already exists", "error.product.already_exists"),
            Map.entry("Product is not active", "error.product.not_active"),
            Map.entry("Invalid metadata format", "error.metadata.invalid_format"),
            Map.entry("Variation already exists", "error.variation.already_exists"),
            Map.entry("Metadata structure must be same across variations", "error.metadata.structure_mismatch_variations"),
            Map.entry("At least one field must be provided for update", "error.update.no_fields"),
            Map.entry("Quantity available must be 0 or more", "error.variation.quantity.invalid"),
            Map.entry("Price must be 0 or more", "error.variation.price.invalid"),
            Map.entry("Product already deleted", "error.product.already_deleted"),
            Map.entry("Variation does not belong to this product", "error.variation.not_in_product"),
            Map.entry("Product is not available", "error.product.not_available"),
            Map.entry("Product has no active variations", "error.product.no_active_variations"),
            Map.entry("Seller account is not activated", "error.seller.not_activated"),
            Map.entry("Customer account is not activated", "error.customer.not_activated"),
            Map.entry("Not your product", "error.product.not_owned"),
            Map.entry("Product is deleted", "error.product.deleted"),
            Map.entry("Not your product variation", "error.variation.not_owned"),
            Map.entry("At least one metadata field is required", "error.metadata.field_required"),
            Map.entry("Invalid metadata field", "error.metadata.field_invalid"),
            Map.entry("Metadata structure mismatch", "error.metadata.structure_mismatch"),
            Map.entry("Invalid primary image format", "error.image.primary.invalid_format"),
            Map.entry("Category name cannot be empty", "error.category.name.empty"),
            Map.entry("Category already exists under this parent", "error.category.already_exists_under_parent"),
            Map.entry("Field name cannot be empty", "error.metadata.field_name.empty"),
            Map.entry("Metadata field already exists", "error.metadata.field_already_exists"),
            Map.entry("At least one value must be provided", "error.metadata.values.required"),
            Map.entry("Duplicate values are not allowed", "error.metadata.values.duplicate"),
            Map.entry("CategoryId is required", "error.category.id.required"),
            Map.entry("Seller can have only one address", "error.address.single_for_seller"),
            Map.entry("Account is locked", "error.account.locked"),
            Map.entry("Old password is incorrect", "error.password.old_incorrect"),
            Map.entry("Passwords do not match", "error.password.mismatch"),
            Map.entry("New password must be different", "error.password.new_must_differ"),
            Map.entry("Email already registered. Please login.", "error.email.already_registered"),
            Map.entry("GST already registered", "error.seller.gst.already_registered"),
            Map.entry("Company name already registered", "error.seller.company.already_registered"),
            Map.entry("Activation token expired. A new activation link has been sent.", "error.activation.expired_resent"),
            Map.entry("Account not activated", "error.account.not_activated"),
            Map.entry("Password expired. Please reset your password.", "error.password.expired"),
            Map.entry("Invalid email or password", "error.auth.invalid_credentials"),
            Map.entry("Account is not activated. Please activate your account first.", "error.account.not_activated_first"),
            Map.entry("Reset token expired", "error.reset.expired"),
            Map.entry("Too many attempts. Request a new reset link.", "error.reset.too_many_attempts"),
            Map.entry("Password does not meet policy requirements", "error.password.policy"),
            Map.entry("Refresh token expired", "error.refresh.expired"),
            Map.entry("Account already activated", "error.account.already_activated"),
            Map.entry("Token missing", "error.token.missing"),
            Map.entry("Failed to store profile image", "error.image.profile.store_failed"),
            Map.entry("Image not found", "error.image.not_found"),
            Map.entry("Failed to store primary image", "error.image.primary.store_failed"),
            Map.entry("Failed to store secondary images", "error.image.secondary.store_failed"),
            Map.entry("Invalid image format", "error.image.invalid_format"),
            Map.entry("Invalid image content type", "error.image.invalid_content_type"),
            Map.entry("Image size exceeds allowed limit", "error.image.size_exceeded"),
            Map.entry("Failed to process image", "error.image.process_failed"),
            Map.entry("Invalid sort field", "error.page.sort.invalid"),
            Map.entry("Cart is empty", "error.cart.empty"),
            Map.entry("Cart item not found", "error.cart.item.not_found"),
            Map.entry("Insufficient stock available", "error.stock.insufficient"),
            Map.entry("Order not found", "error.order.not_found"),
            Map.entry("Order is already cancelled", "error.order.already_cancelled"),
            Map.entry("Completed orders cannot be cancelled", "error.order.cannot_cancel_completed"),
            Map.entry("Order cannot be cancelled after seller confirmation", "error.order.cannot_cancel_after_confirmation"),
            Map.entry("Order contains non-cancellable items", "error.order.not_cancellable"),
            Map.entry("Order is cancelled", "error.order.cancelled"),
            Map.entry("Invalid seller order status transition", "error.order.invalid_status_transition"),
            Map.entry("Online payment is not supported yet", "error.payment.online_not_supported"),
            Map.entry("Quantity must be at least 1", "error.quantity.min_one"),
            Map.entry("User is not a customer", "error.user.not_customer"),
            Map.entry("Product variation is no longer available", "error.variation.unavailable"),
            Map.entry("Seller order not found", "error.order.not_found"),
            Map.entry("Payment is already completed", "error.payment.already_completed"),
            Map.entry("Payment confirmation is only required for online orders", "error.payment.confirm_online_only"),
            Map.entry("Payment cannot be confirmed for this order", "error.payment.cannot_confirm"),
            Map.entry("Order payment is not completed", "error.payment.not_completed"),
            Map.entry("Online payment gateway is not configured", "error.payment.gateway_not_configured"),
            Map.entry("Failed to initiate online payment", "error.payment.init_failed"),
            Map.entry("Invalid payment signature", "error.payment.invalid_signature"),
            Map.entry("Payment order mismatch", "error.payment.order_mismatch"),
            Map.entry("Payment session is only for online orders", "error.payment.session_online_only"),
            Map.entry("Payment verification is only for online orders", "error.payment.verify_online_only"),
            Map.entry("Order amount is too low for online payment", "error.payment.amount_too_low"),
            Map.entry("Item is not returnable", "error.return.not_returnable"),
            Map.entry("Item can only be returned after delivery", "error.return.not_delivered"),
            Map.entry("Return already requested for this item", "error.return.already_requested"),
            Map.entry("Return request not found", "error.return.not_found"),
            Map.entry("Return request is no longer pending", "error.return.not_pending"),
            Map.entry("Invalid return status transition", "error.return.invalid_status")
    );

    private final MessageSource messageSource;

    public MessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String code, String defaultMessage, Object... args) {
        return messageSource.getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    public String resolveExceptionMessage(String message) {
        String code = EXCEPTION_MESSAGE_CODES.get(message);
        if (code == null) {
            return message;
        }
        return get(code, message);
    }
}
