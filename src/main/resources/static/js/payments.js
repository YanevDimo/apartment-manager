/**
 * Payment Management JavaScript
 * Handles payment CRUD operations and integration with apartments
 */

// Global variables
let selectedApartmentForPayment = null;

/**
 * Fallback helpers (when apartments.js is not loaded)
 */
if (typeof showLoading === 'undefined') {
    function showLoading(show) {
        const overlay = $('#loadingOverlay');
        if (!overlay.length) return;
        if (show) {
            overlay.addClass('show');
        } else {
            overlay.removeClass('show');
        }
    }
}

if (typeof showToast === 'undefined') {
    function showToast(message, type = 'info') {
        const bgClass = {
            'success': 'bg-success',
            'error': 'bg-danger',
            'warning': 'bg-warning',
            'info': 'bg-info'
        }[type] || 'bg-info';
        
        const icon = {
            'success': 'bi-check-circle',
            'error': 'bi-x-circle',
            'warning': 'bi-exclamation-triangle',
            'info': 'bi-info-circle'
        }[type] || 'bi-info-circle';
        
        const toast = $(`
            <div class="toast align-items-center text-white ${bgClass} border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">
                        <i class="bi ${icon} me-2"></i>${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `);
        
        $('.toast-container').append(toast);
        const bsToast = new bootstrap.Toast(toast[0]);
        bsToast.show();
        
        toast.on('hidden.bs.toast', function() {
            $(this).remove();
        });
    }
}

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
    
    // Payment amount validation and suggestion
    $('#paymentAmount').on('input', function() {
        validatePaymentAmount();
    });
    
    // Update hint when stage is manually changed
    $('#paymentStage').on('change', function() {
        $('#paymentHint').remove();
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
            
            // Clear previous hints
            $('#paymentHint').remove();
            $('#depositWarning').remove();
            
            // Load payment summary and plan
            loadPaymentSummary(apartmentId);
            
            // Check if deposit already exists
            checkAndDisableDeposit(apartmentId);
            
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
 * Check if deposit exists and disable checkbox if it does
 */
function checkAndDisableDeposit(apartmentId) {
    $.ajax({
        url: `/payments/api/apartment/${apartmentId}/hasDeposit`,
        method: 'GET',
        success: function(data) {
            const depositCheckbox = $('#isDeposit');
            if (data.hasDeposit) {
                depositCheckbox.prop('disabled', true);
                depositCheckbox.prop('checked', false);
                
                // Add warning message if not exists
                if (!$('#depositWarning').length) {
                    depositCheckbox.closest('.form-check').after(
                        '<small id="depositWarning" class="text-warning d-block mt-1">' +
                        '<i class="bi bi-exclamation-triangle me-1"></i>' +
                        'Капаро вече е платено за този обект' +
                        '</small>'
                    );
                }
            } else {
                depositCheckbox.prop('disabled', false);
                $('#depositWarning').remove();
            }
        },
        error: function() {
            // If error, allow checkbox
            $('#isDeposit').prop('disabled', false);
            $('#depositWarning').remove();
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
            
            // Clear previous hints
            $('#paymentHint').remove();
            $('#depositWarning').remove();
            
            // Load payment summary and plan
            loadPaymentSummary(payment.apartment.id);
            
            // Check if deposit exists (but allow editing current deposit)
            const isCurrentlyDeposit = payment.isDeposit || false;
            if (!isCurrentlyDeposit) {
                checkAndDisableDeposit(payment.apartment.id);
            } else {
                // This is a deposit payment being edited, allow checkbox
                $('#isDeposit').prop('disabled', false);
            }
            
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
    
    // Load payment plan details
    loadPaymentPlan(apartmentId);
}

/**
 * Load payment plan details with progress by stage
 */
function loadPaymentPlan(apartmentId) {
    $.ajax({
        url: `/payments/api/apartment/${apartmentId}/payment-plan`,
        method: 'GET',
        success: function(data) {
            if (!data.hasPaymentPlan) {
                $('#paymentPlanSection').hide();
                return;
            }
            
            const stages = [
                { key: 'prelim', name: 'При предварителен договор' },
                { key: 'akt14', name: 'Акт 14' },
                { key: 'akt15', name: 'Акт 15' },
                { key: 'akt16', name: 'Акт 16' }
            ];
            
            const tbody = $('#paymentPlanBody');
            tbody.empty();
            
            let cumulativeOverpayment = 0; // Track overpayment to distribute
            
            stages.forEach((stage, index) => {
                const expected = parseFloat(data.expectedAmounts[stage.key] || 0);
                let paid = parseFloat(data.paidByStage[stage.key] || 0);
                
                // Apply cumulative overpayment from previous stages
                if (cumulativeOverpayment > 0) {
                    const toApply = Math.min(cumulativeOverpayment, Math.max(0, expected - paid));
                    paid += toApply;
                    cumulativeOverpayment -= toApply;
                }
                
                const remaining = Math.max(0, expected - paid);
                const overpayment = Math.max(0, paid - expected);
                
                // Add overpayment to cumulative for next stage
                if (overpayment > 0) {
                    cumulativeOverpayment += overpayment;
                }
                
                let statusBadge = '';
                let rowClass = '';
                
                if (paid >= expected && expected > 0) {
                    statusBadge = '<span class="badge bg-success">Платено</span>';
                    rowClass = 'table-success';
                } else if (paid > 0 && paid < expected) {
                    statusBadge = '<span class="badge bg-warning">Частично</span>';
                    rowClass = 'table-warning';
                } else if (expected > 0) {
                    statusBadge = '<span class="badge bg-secondary">Неплатено</span>';
                } else {
                    statusBadge = '<span class="badge bg-light text-dark">Не се прилага</span>';
                    rowClass = 'table-light';
                }
                
                const row = `
                    <tr class="${rowClass}">
                        <td><strong>${stage.name}</strong></td>
                        <td>${formatCurrency(expected)} €</td>
                        <td>${formatCurrency(paid)} €${overpayment > 0 ? ' <span class="badge bg-info">+' + formatCurrency(overpayment) + '</span>' : ''}</td>
                        <td class="${remaining > 0 ? 'text-danger fw-bold' : ''}">${formatCurrency(remaining)} €</td>
                        <td>${statusBadge}</td>
                    </tr>
                `;
                
                tbody.append(row);
            });
            
            // Show warning for unallocated payments
            const otherPayments = parseFloat(data.paidByStage.other || 0);
            if (otherPayments > 0 || cumulativeOverpayment > 0) {
                const totalUnallocated = otherPayments + cumulativeOverpayment;
                $('#paymentWarningText').html(
                    `<strong>Внимание:</strong> Има ${formatCurrency(totalUnallocated)} € нерапределени плащания. ` +
                    `Моля свържете плащанията с конкретен етап.`
                );
                $('#paymentWarning').show();
            } else {
                $('#paymentWarning').hide();
            }
            
            $('#paymentPlanSection').show();
        },
        error: function() {
            $('#paymentPlanSection').hide();
        }
    });
}

/**
 * Format currency with proper decimal places
 */
function formatCurrency(value) {
    return new Intl.NumberFormat('bg-BG', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(value);
}

/**
 * Format date as dd.MM.yyyy
 */
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    if (Number.isNaN(date.getTime())) return '-';
    return date.toLocaleDateString('bg-BG', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

/**
 * Validate payment amount and suggest stage
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
    
    // Suggest payment stage based on amount
    if (amount > 0 && !paymentId) {
        suggestPaymentStage(amount);
    }
}

/**
 * Suggest payment stage based on payment plan progress
 */
function suggestPaymentStage(amount) {
    const apartmentId = $('#paymentApartmentId').val();
    
    if (!apartmentId) return;
    
    $.ajax({
        url: `/payments/api/apartment/${apartmentId}/payment-plan`,
        method: 'GET',
        success: function(data) {
            if (!data.hasPaymentPlan) return;
            
            const stages = [
                { key: 'prelim', name: 'При предварителен договор', value: 'При предварителен договор' },
                { key: 'akt14', name: 'Акт 14', value: 'Акт 14' },
                { key: 'akt15', name: 'Акт 15', value: 'Акт 15' },
                { key: 'akt16', name: 'Акт 16', value: 'Акт 16' }
            ];
            
            // Find first unpaid or partially paid stage
            let suggestedStage = null;
            let suggestedAmount = 0;
            
            for (const stage of stages) {
                const expected = parseFloat(data.expectedAmounts[stage.key] || 0);
                const paid = parseFloat(data.paidByStage[stage.key] || 0);
                const remaining = expected - paid;
                
                if (remaining > 0.01) {
                    suggestedStage = stage;
                    suggestedAmount = remaining;
                    break;
                }
            }
            
            if (suggestedStage) {
                // Auto-select the suggested stage
                $('#paymentStage').val(suggestedStage.value);
                
                // Show hint
                const diff = amount - suggestedAmount;
                let hintMessage = '';
                
                if (Math.abs(diff) < 0.01) {
                    hintMessage = `<i class="bi bi-check-circle text-success"></i> Плащането покрива точно етап "${suggestedStage.name}"`;
                } else if (diff < 0) {
                    hintMessage = `<i class="bi bi-exclamation-triangle text-warning"></i> Недостига ${formatCurrency(Math.abs(diff))} € за покриване на "${suggestedStage.name}" (очаквано: ${formatCurrency(suggestedAmount)} €)`;
                } else {
                    hintMessage = `<i class="bi bi-info-circle text-info"></i> Надвишение от ${formatCurrency(diff)} € ще се отчете за следващия етап`;
                }
                
                if (!$('#paymentHint').length) {
                    $('#paymentAmount').after('<small id="paymentHint" class="form-text"></small>');
                }
                $('#paymentHint').html(hintMessage);
            }
        }
    });
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
                
                // Clear payment hint
                $('#paymentHint').remove();
                
                // Reload apartments table to update totals
                if (typeof apartmentsTable !== 'undefined') {
                    apartmentsTable.ajax.reload();
                }
                
                // Reload payment plan if viewing payments
                const apartmentId = formData.apartment.id;
                if (apartmentId && $('#viewPaymentsModal').is(':visible')) {
                    viewPaymentsForApartment(apartmentId);
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

    // Search filter on payments list
    $('#paymentSearch').on('input', function() {
        const searchTerm = $(this).val().toLowerCase();
        $('#paymentsListBody tr').each(function() {
            const text = $(this).text().toLowerCase();
            $(this).toggle(text.includes(searchTerm));
        });
    });

    // Load all payments if on payments page
    if ($('#paymentsListBody').length) {
        loadAllPayments();
    }
});

/**
 * Load all payments for payments page
 */
function loadAllPayments() {
    showLoading(true);
    
    $.ajax({
        url: '/payments/api/list',
        method: 'GET',
        success: function(data) {
            const tbody = $('#paymentsListBody');
            tbody.empty();
            
            if (!data.payments || data.payments.length === 0) {
                tbody.append('<tr><td colspan="8" class="text-center text-muted">Няма плащания</td></tr>');
            } else {
                const totalAmount = data.payments.reduce((sum, p) => sum + parseFloat(p.amount || 0), 0);
                const bankTotal = data.payments
                    .filter(p => p.paymentMethod === 'Банка')
                    .reduce((sum, p) => sum + parseFloat(p.amount || 0), 0);
                const cashTotal = data.payments
                    .filter(p => p.paymentMethod === 'В брой')
                    .reduce((sum, p) => sum + parseFloat(p.amount || 0), 0);

                $('#totalPaymentsCount').text(data.total || data.payments.length || 0);
                $('#totalPaymentsAmount').text(formatCurrency(totalAmount) + ' €');
                $('#totalBankPayments').text(formatCurrency(bankTotal) + ' €');
                $('#totalCashPayments').text(formatCurrency(cashTotal) + ' €');

                data.payments.forEach(payment => {
                    const row = `
                        <tr>
                            <td>${payment.apartment || '-'}</td>
                            <td class="fw-bold">${formatCurrency(parseFloat(payment.amount || 0))} €</td>
                            <td>${formatDate(payment.paymentDate) || '-'}</td>
                            <td>${payment.paymentMethod || '-'}</td>
                            <td>${payment.paymentStage || '-'}</td>
                            <td>${payment.invoiceNumber || '-'}</td>
                            <td>${payment.isDeposit ? '<span class="badge bg-info">Да</span>' : '<span class="badge bg-secondary">Не</span>'}</td>
                            <td>
                                <button class="btn btn-sm btn-outline-primary btn-edit-payment" data-id="${payment.id}" title="Редактирай">
                                    <i class="bi bi-pencil"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger btn-delete-payment" data-id="${payment.id}" title="Изтрий">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </td>
                        </tr>
                    `;
                    tbody.append(row);
                });
            }
            
            showLoading(false);
        },
        error: function() {
            showToast('Грешка при зареждане на плащанията', 'error');
            showLoading(false);
        }
    });
}





