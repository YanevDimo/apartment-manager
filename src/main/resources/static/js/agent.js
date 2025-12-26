// Agent panel JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Property form with image preview
    const imagesInput = document.getElementById('images');
    if (imagesInput) {
        imagesInput.addEventListener('change', function(e) {
            const files = e.target.files;
            if (files.length > 0) {
                showImagePreview(files);
            }
        });
    }

    // Property form submission
    const propertyForm = document.getElementById('propertyForm');
    if (propertyForm) {
        propertyForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(propertyForm);
            const submitButton = propertyForm.querySelector('button[type="submit"]');
            const originalText = submitButton.innerHTML;
            submitButton.disabled = true;
            submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Запазване...';
            
            const url = propertyForm.getAttribute('action');
            
            fetch(url, {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (response.redirected) {
                    window.location.href = response.url;
                } else {
                    return response.text();
                }
            })
            .then(html => {
                if (html) {
                    // If we get HTML back, there might be validation errors
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    const errors = doc.querySelectorAll('.text-danger');
                    if (errors.length > 0) {
                        showToast('Моля, попълнете всички задължителни полета', 'error');
                    }
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('Грешка при запазване на имот', 'error');
            })
            .finally(() => {
                submitButton.disabled = false;
                submitButton.innerHTML = originalText;
            });
        });
    }
});

function showImagePreview(files) {
    const previewContainer = document.getElementById('imagePreview') || createImagePreviewContainer();
    previewContainer.innerHTML = '';
    
    Array.from(files).forEach(file => {
        if (file.type.startsWith('image/')) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.className = 'img-thumbnail me-2 mb-2';
                img.style.maxWidth = '150px';
                img.style.maxHeight = '150px';
                previewContainer.appendChild(img);
            };
            reader.readAsDataURL(file);
        }
    });
}

function createImagePreviewContainer() {
    const container = document.createElement('div');
    container.id = 'imagePreview';
    container.className = 'mt-3';
    const imagesInput = document.getElementById('images');
    if (imagesInput && imagesInput.parentNode) {
        imagesInput.parentNode.appendChild(container);
    }
    return container;
}

function showToast(message, type) {
    const toastContainer = document.getElementById('toastContainer') || createToastContainer();
    
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    
    toastContainer.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    
    toast.addEventListener('hidden.bs.toast', () => {
        toast.remove();
    });
}

function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    container.style.zIndex = '9999';
    document.body.appendChild(container);
    return container;
}



