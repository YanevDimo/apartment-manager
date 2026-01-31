/**
 * Apartments Management JavaScript
 * Handles table, row selection, color coding, and CRUD operations
 */

// Global variables
let apartmentsTable;
let selectedApartmentId = null;
let clients = [];

// Initialize on document ready
$(document).ready(function() {
    initializeTable();
    loadClients();
    setupEventHandlers();
    openModalFromQuery();
});

/**
 * Initialize DataTables with row selection
 */
function initializeTable() {
    apartmentsTable = $('#apartmentsTable').DataTable({
        language: {
            url: '//cdn.datatables.net/plug-ins/1.13.6/i18n/bg.json'
        },
        processing: true,
        serverSide: false,
        ajax: {
            url: '/apartments/api/list',
            dataSrc: function(json) {
                $('#totalBadge').text(json.total || 0);
                return json.apartments || [];
            }
        },
            columns: [
            { data: 'id', visible: false },
            { 
                data: 'apartmentNumber',
                render: function(data, type, row) {
                    return `<a href="#" class="view-payments-link" data-id="${row.id}" title="Виж плащания">${data}</a>`;
                }
            },
            { 
                data: 'area',
                render: function(data) {
                    return parseFloat(data || 0).toFixed(2);
                }
            },
            { 
                data: 'pricePerM2',
                render: function(data) {
                    return parseFloat(data || 0).toFixed(2);
                }
            },
            { 
                data: 'totalPrice',
                render: function(data) {
                    return parseFloat(data || 0).toFixed(2);
                }
            },
            { 
                data: 'stage',
                render: function(data) {
                    return data || '-';
                }
            },
            { 
                data: 'client',
                render: function(data) {
                    return data || '-';
                }
            },
            { 
                data: 'totalPaid',
                render: function(data) {
                    return parseFloat(data || 0).toFixed(2);
                }
            },
            { 
                data: 'remainingPayment',
                render: function(data) {
                    return parseFloat(data || 0).toFixed(2);
                }
            },
            {
                data: 'hasOverduePayments',
                render: function(data) {
                    if (data) {
                        return '<span class="badge badge-overdue">Изоставащо</span>';
                    }
                    return '<span class="badge badge-paid">Навреме</span>';
                }
            },
            {
                data: null,
                orderable: false,
                render: function(data, type, row) {
                    return `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-primary btn-edit" data-id="${row.id}" title="Редактирай">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-sm btn-danger btn-delete" data-id="${row.id}" title="Изтрий">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    `;
                }
            }
        ],
        rowCallback: function(row, data) {
            // Add color coding based on overdue payments
            $(row).removeClass('overdue');
            if (data.hasOverduePayments) {
                $(row).addClass('overdue');
            }
            
            // Set row key for stable selection (using index)
            $(row).attr('data-row-key', data.id);
        },
        createdRow: function(row, data, dataIndex) {
            // Handle row click for selection
            $(row).on('click', function(e) {
                // Don't select if clicking on action buttons or links
                if ($(e.target).closest('.action-buttons, a, button').length) {
                    return;
                }
                
                // Remove previous selection
                $('#apartmentsTable tbody tr').removeClass('selected');
                
                // Add selection to current row
                $(row).addClass('selected');
                selectedApartmentId = data.id;
                
                // Enable add payment button
                $('#btnAddPayment').prop('disabled', false);
                
                // Trigger event for payment management
                $(document).trigger('apartmentSelected', [data.id]);
            });
        },
        order: [[1, 'asc']], // Sort by apartment number
        pageLength: 25,
        responsive: true,
        scrollX: true,
        stateSave: true // Save table state (selection, pagination, etc.)
    });
}

/**
 * Load clients for dropdown
 */
function loadClients() {
    $.ajax({
        url: '/apartments/api/clients',
        method: 'GET',
        success: function(data) {
            clients = data.clients || [];
            populateClientSelect();
        },
        error: function() {
            console.error('Error loading clients');
            // Try alternative endpoint
            $.ajax({
                url: '/api/clients',
                method: 'GET',
                success: function(data) {
                    clients = Array.isArray(data) ? data : [];
                    populateClientSelect();
                },
                error: function() {
                    console.error('Failed to load clients from all endpoints');
                    clients = [];
                }
            });
        }
    });
}

/**
 * Populate client select dropdown
 */
function populateClientSelect() {
    const select = $('#clientSelect');
    select.empty();
    select.append('<option value="">Изберете клиент</option>');
    
    clients.forEach(client => {
        const name = client.name || '';
        select.append(`<option value="${client.id}">${name}</option>`);
    });
}

/**
 * Setup event handlers
 */
function setupEventHandlers() {
    // Add apartment button
    $('#btnAddApartment').on('click', function() {
        openAddModal();
    });
    
    // Edit apartment button (from table)
    $(document).on('click', '.btn-edit', function(e) {
        e.stopPropagation();
        const id = $(this).data('id');
        openEditModal(id);
    });
    
    // Delete apartment button
    $(document).on('click', '.btn-delete', function(e) {
        e.stopPropagation();
        const id = $(this).data('id');
        deleteApartment(id);
    });
    
    // Save apartment form
    $('#btnSaveApartment').on('click', function() {
        saveApartment();
    });
    
    // Calculate total price on input change
    $('#area').on('input', function() {
        calculateTotalPrice();
    });
    
    // Overdue report button
    $('#btnOverdueReport').on('click', function() {
        showOverdueReport();
    });
    
    // Global stage change button
    $('#btnGlobalStage').on('click', function() {
        $('#globalStageModal').modal('show');
    });
    
    // View payments link
    $(document).on('click', '.view-payments-link', function(e) {
        e.preventDefault();
        e.stopPropagation();
        const apartmentId = $(this).data('id');
        if (typeof viewPaymentsForApartment === 'function') {
            viewPaymentsForApartment(apartmentId);
        }
    });
    
    // Save global stage
    $('#btnSaveGlobalStage').on('click', function() {
        saveGlobalStage();
    });
}

/**
 * Auto-open add modal from query param
 */
function openModalFromQuery() {
    const params = new URLSearchParams(window.location.search);
    const clientId = params.get('client');
    if (params.get('add') === '1') {
        openAddModal(clientId);
        params.delete('add');
        params.delete('client');
        const newUrl = window.location.pathname + (params.toString() ? '?' + params.toString() : '');
        window.history.replaceState({}, document.title, newUrl);
    }
}

/**
 * Open add apartment modal
 * @param {string} preselectedClientId - Optional client ID to preselect in dropdown
 */
function openAddModal(preselectedClientId) {
    $('#apartmentModalLabel').html('<i class="bi bi-plus-circle me-2"></i>Добави обект');
    $('#apartmentForm')[0].reset();
    $('#apartmentId').val('');
    $('#totalPriceAlert').hide();
    
    // Preselect client if provided
    if (preselectedClientId && $('#clientSelect').length) {
        $('#clientSelect').val(preselectedClientId);
    }
    
    $('#apartmentModal').modal('show');
}

/**
 * Open edit apartment modal
 */
function openEditModal(id) {
    showLoading(true);
    
    $.ajax({
        url: `/apartments/api/${id}`,
        method: 'GET',
        success: function(data) {
            $('#apartmentModalLabel').html('<i class="bi bi-pencil me-2"></i>Редактирай обект');
            $('#apartmentId').val(data.id);
            $('#apartmentNumber').val(data.apartmentNumber);
            $('#area').val(data.area);
            $('#stage').val(data.stage || '');
            $('#notes').val(data.notes || '');
            
            calculateTotalPrice();
            $('#apartmentModal').modal('show');
            showLoading(false);
        },
        error: function() {
            showToast('Грешка при зареждане на данните', 'error');
            showLoading(false);
        }
    });
}

/**
 * Save apartment (add or update)
 */
function saveApartment() {
    const form = $('#apartmentForm')[0];
    
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }
    
    const formData = {
        id: $('#apartmentId').val() || null,
        apartmentNumber: $('#apartmentNumber').val(),
        area: parseFloat($('#area').val()),
        stage: $('#stage').val() || null,
        notes: $('#notes').val() || null,
        isSold: true
    };
    
    showLoading(true);
    
    const isEdit = formData.id !== null && formData.id !== '';
    const url = isEdit ? `/apartments/api/update/${formData.id}` : '/apartments/api/add';
    const method = isEdit ? 'PUT' : 'POST';
    
    $.ajax({
        url: url,
        method: method,
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function(response) {
            if (response.success) {
                showToast(response.message || 'Апартаментът е запазен успешно', 'success');
                $('#apartmentModal').modal('hide');
                apartmentsTable.ajax.reload();
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
 * Delete apartment
 */
function deleteApartment(id) {
    if (!confirm('Сигурни ли сте, че искате да изтриете този апартамент?')) {
        return;
    }
    
    showLoading(true);
    
    $.ajax({
        url: `/apartments/api/delete/${id}`,
        method: 'DELETE',
        success: function(response) {
            if (response.success) {
                showToast(response.message || 'Апартаментът е изтрит успешно', 'success');
                apartmentsTable.ajax.reload();
                selectedApartmentId = null;
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
 * Show overdue payments report
 */
function showOverdueReport() {
    showLoading(true);
    
    $.ajax({
        url: '/apartments/api/overdue',
        method: 'GET',
        success: function(data) {
            const tbody = $('#overdueTableBody');
            tbody.empty();
            
            if (!data || data.length === 0) {
                tbody.append('<tr><td colspan="5" class="text-center text-muted">Няма апартаменти с изостанали плащания</td></tr>');
            } else {
                data.forEach(apt => {
                    const row = `
                        <tr>
                            <td>${apt.apartmentNumber || '-'}</td>
                            <td>${apt.client ? apt.client.name : '-'}</td>
                            <td>${apt.stage || '-'}</td>
                            <td class="text-danger fw-bold">${parseFloat(apt.remainingPayment || 0).toFixed(2)}</td>
                            <td>
                                <button class="btn btn-sm btn-primary btn-edit" data-id="${apt.id}">
                                    <i class="bi bi-pencil"></i> Редактирай
                                </button>
                            </td>
                        </tr>
                    `;
                    tbody.append(row);
                });
            }
            
            $('#overdueModal').modal('show');
            showLoading(false);
        },
        error: function() {
            showToast('Грешка при зареждане на изостаналите плащания', 'error');
            showLoading(false);
        }
    });
}

/**
 * Save global stage change
 */
function saveGlobalStage() {
    const stage = $('#globalStage').val();
    
    if (!stage) {
        alert('Моля, изберете етап');
        return;
    }
    
    if (!confirm(`Сигурни ли сте, че искате да промените етапа на ВСИЧКИ апартаменти на "${stage}"?`)) {
        return;
    }
    
    showLoading(true);
    
    $.ajax({
        url: '/apartments/api/stage/global',
        method: 'POST',
        data: { stage: stage },
        success: function(response) {
            if (response.success) {
                showToast(response.message || 'Етапът е променен успешно', 'success');
                $('#globalStageModal').modal('hide');
                apartmentsTable.ajax.reload();
            } else {
                showToast(response.message || 'Грешка при промяна на етапа', 'error');
            }
            showLoading(false);
        },
        error: function(xhr) {
            let errorMessage = 'Грешка при промяна на етапа';
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }
            showToast(errorMessage, 'error');
            showLoading(false);
        }
    });
}

/**
 * Calculate total price preview
 */
function calculateTotalPrice() {
    const area = parseFloat($('#area').val()) || 0;
    const pricePerM2 = parseFloat($('#pricePerM2').val()) || 0;
    const totalPrice = area * pricePerM2;
    
    if (totalPrice > 0) {
        $('#totalPricePreview').text(totalPrice.toFixed(2));
        $('#totalPriceAlert').show();
    } else {
        $('#totalPriceAlert').hide();
    }
}

// Excel import for apartments
$(document).on('submit', '#excelImportForm', function(e) {
    e.preventDefault();
    const formData = new FormData(this);
    const importBtn = $('#excelImportBtn');
    const originalText = importBtn.html();
    importBtn.prop('disabled', true).html('<i class="bi bi-hourglass-split me-2"></i>Импортиране...');
    $('#excelImportResults').hide();

    $.ajax({
        url: '/excel/import',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            importBtn.prop('disabled', false).html(originalText);
            const alertDiv = $('#excelImportAlert');
            const messageSpan = $('#excelImportMessage');
            const iconSpan = $('#excelImportIcon');
            const detailsDiv = $('#excelImportDetails');

            if (response.success) {
                alertDiv.removeClass('alert-danger').addClass('alert-success');
                iconSpan.removeClass('bi-x-circle').addClass('bi-check-circle');
                messageSpan.text('Импортът е завършен успешно!');
                let details = '<ul class="mb-0 mt-2">';
                details += '<li><strong>Импортирани:</strong> ' + (response.imported || 0) + ' обекта</li>';
                details += '<li><strong>Пропуснати:</strong> ' + (response.skipped || 0) + ' обекта</li>';
                details += '</ul>';
                if (response.errors && response.errors.length > 0) {
                    details += '<div class="mt-3"><strong>Грешки:</strong><ul class="mb-0">';
                    response.errors.forEach(function(error) {
                        details += '<li class="text-danger small">' + error + '</li>';
                    });
                    details += '</ul></div>';
                }
                detailsDiv.html(details);
                apartmentsTable.ajax.reload();
            } else {
                alertDiv.removeClass('alert-success').addClass('alert-danger');
                iconSpan.removeClass('bi-check-circle').addClass('bi-x-circle');
                messageSpan.text('Грешка при импорт: ' + (response.message || 'Неизвестна грешка'));
                detailsDiv.html('');
            }
            $('#excelImportResults').show();
        },
        error: function(xhr) {
            importBtn.prop('disabled', false).html(originalText);
            const alertDiv = $('#excelImportAlert');
            const messageSpan = $('#excelImportMessage');
            const iconSpan = $('#excelImportIcon');
            alertDiv.removeClass('alert-success').addClass('alert-danger');
            iconSpan.removeClass('bi-check-circle').addClass('bi-x-circle');
            let errorMessage = 'Грешка при импорт: ';
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage += xhr.responseJSON.message;
            } else {
                errorMessage += 'Неизвестна грешка';
            }
            messageSpan.text(errorMessage);
            $('#excelImportDetails').html('');
            $('#excelImportResults').show();
        }
    });
});

/**
 * Show/hide loading overlay
 */
function showLoading(show) {
    if (show) {
        $('#loadingOverlay').addClass('show');
    } else {
        $('#loadingOverlay').removeClass('show');
    }
}

/**
 * Show toast notification
 */
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

