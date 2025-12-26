/**
 * Payment Management JavaScript
 * Handles payment CRUD operations and integration with apartments
 */

// Global variables
let selectedApartmentForPayment = null;

/**
 * Initialize payment functionality
 */
function initializePayments() {
    // Add payment button handler
    $('#btnAddPayment').on('click', function() {
        if (selectedApartmentId) {
            openAddPaymentModal(selectedApartmentId);
        }
    });
    
    // Save payment form
    $('#btnSavePayment').on('click', function() {
        savePayment();
    });
    
    // Payment amount validation
    $('#paymentAmount').on('input', function() {
        validatePaymentAmount();
    });
    
    // Set default date to today
    $('#paymentDate').val(new Date().toISOString().split('T')[0]);
}

/**
 * Open add payment modal
 */
function openAddPaymentModal(apartmentId) {
    selectedApartmentForPayment = apartmentId;
    
    // Load apartment info
    $.ajax({
        url: `/apartments/api/${apartmentId}`,
        method: 'GET',
        success: function(apartment) {
            $('#paymentModalLabel').html('<i class="bi bi-cash-coin me-2"></i>Добави плащане');
            $('#paymentForm')[0].reset();
            $('#paymentId').val('');
            $('#paymentApartmentId').val(apartmentId);
            $('#apartmentInfoText').text(apartment.buildingName + ' - ' + apartment.apartmentNumber);
            
            // Load payment summary
            loadPaymentSummary(apartmentId);
            
            // Set default date to today
            $('#paymentDate').val(new Date().toISOString().split('T')[0]);
            
            $('#paymentModal').modal('show');
        },
        error: function() {
            showToast('Грешка при зареждане на данните за апартамента', 'error');
        }
    });
}

/**
 * Open edit payment modal
 */
function openEditPaymentModal(paymentId) {
    $.ajax({
        url: `/payments/api/${paymentId}`,
        method: 'GET',
        success: function(payment) {
            $('#paymentModalLabel').html('<i class="bi bi-pencil me-2"></i>Редактирай плащане');
            $('#paymentId').val(payment.id);
            $('#paymentApartmentId').val(payment.apartment.id);
            $('#paymentAmount').val(payment.amount);
            $('#paymentDate').val(payment.paymentDate);
            $('#paymentMethod').val(payment.paymentMethod || '');
            $('#paymentStage').val(payment.paymentStage || '');
            $('#invoiceNumber').val(payment.invoiceNumber || '');
            $('#isDeposit').prop('checked', payment.isDeposit || false);
            $('#paymentNotes').val(payment.notes || '');
            
            $('#apartmentInfoText').text(payment.apartment.buildingName + ' - ' + payment.apartment.apartmentNumber);
            
            // Load payment summary
            loadPaymentSummary(payment.apartment.id);
            
            $('#paymentModal').modal('show');
        },
        error: function() {
            showToast('Грешка при зареждане на данните за плащането', 'error');
        }
    });
}

/**
 * Load payment summary for apartment
 */
function loadPaymentSummary(apartmentId) {
    $.ajax({
        url: `/payments/api/apartment/${apartmentId}/total`,
        method: 'GET',
        success: function(data) {
            $('#totalPriceInfo').text(parseFloat(data.totalPrice || 0).toFixed(2));
            $('#totalPaidInfo').text(parseFloat(data.totalPaid || 0).toFixed(2));
            $('#remainingInfo').text(parseFloat(data.remainingPayment || 0).toFixed(2));
            $('#paymentSummary').show();
        },
        error: function() {
            $('#paymentSummary').hide();
        }
    });
}

/**
 * Validate payment amount
 */
function validatePaymentAmount() {
    const amount = parseFloat($('#paymentAmount').val()) || 0;
    const remaining = parseFloat($('#remainingInfo').text().replace(' €', '').replace(',', '')) || 0;
    const paymentId = $('#paymentId').val();
    
    // If editing, we need to add back the current payment amount
    // This is handled on the server side, so we just show a warning
    if (amount > remaining && !paymentId) {
        $('#paymentAmount').addClass('is-invalid');
        $('#paymentAmount').next('.invalid-feedback').text('Сумата надвишава остатъка за плащане');
    } else {
        $('#paymentAmount').removeClass('is-invalid');
    }
}

/**
 * Save payment (add or update)
 */
function savePayment() {
    const form = $('#paymentForm')[0];
    
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }
    
    const formData = {
        id: $('#paymentId').val() || null,
        amount: parseFloat($('#paymentAmount').val()),
        paymentDate: $('#paymentDate').val(),
        paymentMethod: $('#paymentMethod').val() || null,
        paymentStage: $('#paymentStage').val() || null,
        invoiceNumber: $('#invoiceNumber').val() || null,
        isDeposit: $('#isDeposit').is(':checked'),
        notes: $('#paymentNotes').val() || null,
        apartment: {
            id: parseInt($('#paymentApartmentId').val())
        }
    };
    
    showLoading(true);
    
    const isEdit = formData.id !== null && formData.id !== '';
    const url = isEdit ? `/payments/api/update/${formData.id}` : '/payments/api/add';
    const method = isEdit ? 'PUT' : 'POST';
    
    $.ajax({
        url: url,
        method: method,
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function(response) {
            if (response.success) {
                showToast(response.message || 'Плащането е запазено успешно', 'success');
                $('#paymentModal').modal('hide');
                
                // Reload apartments table to update totals
                if (typeof apartmentsTable !== 'undefined') {
                    apartmentsTable.ajax.reload();
                }
            } else {
                showToast(response.message || 'Грешка при запазване', 'error');
            }
            showLoading(false);
        },
        error: function(xhr) {
            let errorMessage = 'Грешка при запазване';
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }
            showToast(errorMessage, 'error');
            showLoading(false);
        }
    });
}

/**
 * Delete payment
 */
function deletePayment(paymentId) {
    if (!confirm('Сигурни ли сте, че искате да изтриете това плащане?')) {
        return;
    }
    
    showLoading(true);
    
    $.ajax({
        url: `/payments/api/delete/${paymentId}`,
        method: 'DELETE',
        success: function(response) {
            if (response.success) {
                showToast(response.message || 'Плащането е изтрито успешно', 'success');
                
                // Reload payments if modal is open
                const apartmentId = $('#paymentApartmentId').val();
                if (apartmentId) {
                    viewPaymentsForApartment(apartmentId);
                }
                
                // Reload apartments table
                if (typeof apartmentsTable !== 'undefined') {
                    apartmentsTable.ajax.reload();
                }
            } else {
                showToast(response.message || 'Грешка при изтриване', 'error');
            }
            showLoading(false);
        },
        error: function(xhr) {
            let errorMessage = 'Грешка при изтриване';
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }
            showToast(errorMessage, 'error');
            showLoading(false);
        }
    });
}

/**
 * View payments for apartment
 */
function viewPaymentsForApartment(apartmentId) {
    showLoading(true);
    
    $.ajax({
        url: `/payments/api/by-apartment/${apartmentId}`,
        method: 'GET',
        success: function(data) {
            const tbody = $('#paymentsTableBody');
            tbody.empty();
            
            if (!data.payments || data.payments.length === 0) {
                tbody.append('<tr><td colspan="7" class="text-center text-muted">Няма плащания за този апартамент</td></tr>');
            } else {
                data.payments.forEach(payment => {
                    const row = `
                        <tr>
                            <td>${payment.paymentDate}</td>
                            <td class="fw-bold">${parseFloat(payment.amount || 0).toFixed(2)}</td>
                            <td>${payment.paymentMethod || '-'}</td>
                            <td>${payment.paymentStage || '-'}</td>
                            <td>${payment.invoiceNumber || '-'}</td>
                            <td>${payment.isDeposit ? '<span class="badge bg-info">Да</span>' : '<span class="badge bg-secondary">Не</span>'}</td>
                            <td>
                                <button class="btn btn-sm btn-primary btn-edit-payment" data-id="${payment.id}" title="Редактирай">
                                    <i class="bi bi-pencil"></i>
                                </button>
                                <button class="btn btn-sm btn-danger btn-delete-payment" data-id="${payment.id}" title="Изтрий">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </td>
                        </tr>
                    `;
                    tbody.append(row);
                });
                
                // Add summary row
                const summaryRow = `
                    <tr class="table-info fw-bold">
                        <td colspan="1">Общо:</td>
                        <td>${parseFloat(data.totalPaid || 0).toFixed(2)}</td>
                        <td colspan="2">Остатък:</td>
                        <td colspan="3">${parseFloat(data.remainingPayment || 0).toFixed(2)} €</td>
                    </tr>
                `;
                tbody.append(summaryRow);
            }
            
            $('#viewPaymentsModalLabel').html(`<i class="bi bi-list-ul me-2"></i>Плащания: ${data.apartment}`);
            $('#viewPaymentsModal').modal('show');
            showLoading(false);
        },
        error: function() {
            showToast('Грешка при зареждане на плащанията', 'error');
            showLoading(false);
        }
    });
}

/**
 * Setup payment event handlers
 */
$(document).ready(function() {
    initializePayments();
    
    // Edit payment button
    $(document).on('click', '.btn-edit-payment', function(e) {
        e.stopPropagation();
        const id = $(this).data('id');
        openEditPaymentModal(id);
    });
    
    // Delete payment button
    $(document).on('click', '.btn-delete-payment', function(e) {
        e.stopPropagation();
        const id = $(this).data('id');
        deletePayment(id);
    });
    
    // Update add payment button state when apartment is selected
    if (typeof selectedApartmentId !== 'undefined') {
        // This will be called from apartments.js when row is selected
        $(document).on('apartmentSelected', function(e, apartmentId) {
            $('#btnAddPayment').prop('disabled', !apartmentId);
        });
    }
});





