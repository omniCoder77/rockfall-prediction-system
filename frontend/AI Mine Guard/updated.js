// User management (simple localStorage-based)
const USER_KEY = 'rockfall_users';
const CURRENT_USER_KEY = 'rockfall_current_user';

// Initialize users if not exists
if (!localStorage.getItem(USER_KEY)) {
    localStorage.setItem(USER_KEY, JSON.stringify({}));
}

// Page Management
document.addEventListener('DOMContentLoaded', function() {
    // Initialize the app
    initApp();
    
    // Set up event listeners
    setupEventListeners();
    
    // Update time initially and set interval
    updateAllClocks();
    setInterval(updateAllClocks, 60000);
});

function initApp() {
    // Check if user is already logged in
    const currentUser = localStorage.getItem(CURRENT_USER_KEY);
    if (currentUser) {
        const userData = JSON.parse(currentUser);
        updateUserInfo(userData.name, userData.role);
        showPage('dashboard-page');
    }
    
    // Check for saved theme preference or default to dark
    const savedTheme = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeToggleIcon(savedTheme);
    
    // Set up sidebar toggle functionality
    const toggles = document.querySelectorAll(".nav-toggle");
    
    toggles.forEach((toggle) => {
        toggle.addEventListener("click", () => {
            const parent = toggle.closest(".nav-item");
            const submenu = parent.querySelector(".nav-submenu");
            const isOpen = parent.classList.contains("open");
            
            // Close all open menus first
            document.querySelectorAll(".nav-item.open").forEach((item) => {
                item.classList.remove("open");
                item.querySelector(".nav-toggle").setAttribute("aria-expanded", "false");
            });
            
            // Toggle current one
            if (!isOpen) {
                parent.classList.add("open");
                toggle.setAttribute("aria-expanded", "true");
            } else {
                parent.classList.remove("open");
                toggle.setAttribute("aria-expanded", "false");
            }
        });
    });
    
    // Initialize station data
    initializeStations();
    
    // Set up severity card hover functionality
    setupSeverityCardHover();
}

function setupEventListeners() {
    // Auth tab switching
    document.querySelectorAll('.auth-tab').forEach(tab => {
        tab.addEventListener('click', function() {
            const tabName = this.getAttribute('data-tab');
            
            // Update active tab
            document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            
            // Show correct form
            document.getElementById('login-form').classList.toggle('hidden', tabName !== 'login');
            document.getElementById('signup-form').classList.toggle('hidden', tabName !== 'signup');
        });
    });
    
    // Login form submission
    document.getElementById('login-form').addEventListener('submit', function(e) {
        e.preventDefault();
        
        const name = document.getElementById('name').value;
        const password = document.getElementById('password').value;
        
        // Check credentials
        if (validateLogin(name, password)) {
            // Update user info in all pages
            const userData = getUserData(name);
            updateUserInfo(name, userData.role);
            
            // Save current user
            localStorage.setItem(CURRENT_USER_KEY, JSON.stringify({name, ...userData}));
            
            // Show dashboard
            showPage('dashboard-page');
        } else {
            alert('Invalid credentials. Please check your name and password.');
        }
    });
    
    // Signup form submission
    document.getElementById('signup-form').addEventListener('submit', function(e) {
        e.preventDefault();
        
        const name = document.getElementById('signup-name').value;
        const password = document.getElementById('signup-password').value;
        const confirmPassword = document.getElementById('signup-confirm-password').value;
        const email = document.getElementById('signup-email').value;
        const phone = document.getElementById('signup-phone').value;
        const id = document.getElementById('signup-id').value;
        const role = document.getElementById('signup-role').value;
        
        // Check if passwords match
        if (password !== confirmPassword) {
            alert('Passwords do not match. Please make sure both passwords are identical.');
            return;
        }
        
        // Create new user
        if (createUser(name, password, email, phone, id, role)) {
            alert('Account created successfully! Please login with your credentials.');
            
            // Switch to login tab
            document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
            document.querySelector('.auth-tab[data-tab="login"]').classList.add('active');
            document.getElementById('login-form').classList.remove('hidden');
            document.getElementById('signup-form').classList.add('hidden');
            
            // Pre-fill login form
            document.getElementById('name').value = name;
            document.getElementById('password').value = '';
        } else {
            alert('name already exists. Please choose a different name.');
        }
    });
    
    // Navigation links
    document.querySelectorAll('[data-page]').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const page = this.getAttribute('data-page');
            showPage(page + '-page');
            
            // Update active state in navigation
            document.querySelectorAll('.nav-link').forEach(nav => {
                nav.classList.remove('active');
            });
            this.classList.add('active');
            
            // For submenu items, also activate parent
            if (this.classList.contains('submenu-link')) {
                const parentItem = this.closest('.nav-item');
                parentItem.querySelector('.nav-link').classList.add('active');
            }
        });
    });
    
    // Theme toggle
    document.getElementById('themeToggle').addEventListener('click', toggleTheme);
    
    // Logout button
    document.getElementById('logoutButton').addEventListener('click', logout);
    
    // Avatar editing
    document.getElementById('userAvatar').addEventListener('click', function() {
        document.getElementById('avatarModal').style.display = 'flex';
    });
    
    // name editing
    document.getElementById('editname').addEventListener('click', function() {
        document.getElementById('nameModal').style.display = 'flex';
        document.getElementById('newname').value = document.getElementById('name').textContent;
    });
    
    // Close modals
    document.querySelectorAll('.close-modal').forEach(btn => {
        btn.addEventListener('click', function() {
            this.closest('.modal').style.display = 'none';
        });
    });
    
    // Save avatar
    document.getElementById('saveAvatar').addEventListener('click', saveAvatar);
    
    // Save name
    document.getElementById('savename').addEventListener('click', savename);
    
    // Avatar upload preview
    document.getElementById('avatarUpload').addEventListener('change', previewAvatar);
    
    // Menu toggle for mobile
    document.getElementById('menuToggle').addEventListener('click', function() {
        document.getElementById('sidebar').classList.toggle('active');
    });
    
    // Heatmap controls
    document.getElementById('refreshHeatmap').addEventListener('click', refreshHeatmap);
    document.getElementById('downloadHeatmap').addEventListener('click', downloadHeatmap);
    document.getElementById('fullscreenHeatmap').addEventListener('click', openFullscreenHeatmap);
    document.getElementById('closeFullscreen').addEventListener('click', closeFullscreenHeatmap);
    document.getElementById('downloadFullscreen').addEventListener('click', downloadFullscreenHeatmap);
    
    // Station refresh
    document.getElementById('refreshStations').addEventListener('click', refreshStationData);
}

function showPage(pageId) {
    // Hide all pages
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    
    // Show requested page
    document.getElementById(pageId).classList.add('active');
}

function updateUserInfo(name, role) {
    // Update user info in all elements
    document.querySelectorAll('#name').forEach(element => {
        element.textContent = name;
    });
    
    document.querySelectorAll('#userRole').forEach(element => {
        element.textContent = role;
    });
    
    document.querySelectorAll('#avatarInitials').forEach(element => {
        element.textContent = name.charAt(0).toUpperCase();
    });
    
    document.querySelectorAll('#previewInitials').forEach(element => {
        element.textContent = name.charAt(0).toUpperCase();
    });
    
    document.querySelectorAll('#welcomeName').forEach(element => {
        element.textContent = name;
    });
}

function updateAllClocks() {
    const now = new Date();
    const options = { hour: '2-digit', minute: '2-digit', hour12: true };
    const timeString = now.toLocaleTimeString('en-US', options);
    
    document.querySelectorAll('.time').forEach(element => {
        element.textContent = timeString;
    });
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeToggleIcon(newTheme);
}

function updateThemeToggleIcon(theme) {
    const icon = document.querySelector('#themeToggle i');
    if (theme === 'dark') {
        icon.className = 'fas fa-moon';
    } else {
        icon.className = 'fas fa-sun';
    }
}

function previewAvatar() {
    const file = document.getElementById('avatarUpload').files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const preview = document.getElementById('avatarPreview');
            preview.innerHTML = `<img src="${e.target.result}" alt="Avatar Preview" style="width:100%;height:100%;object-fit:cover;">`;
        };
        reader.readAsDataURL(file);
    }
}

function saveAvatar() {
    const fileInput = document.getElementById('avatarUpload');
    if (fileInput.files.length > 0) {
        const file = fileInput.files[0];
        const reader = new FileReader();
        reader.onload = function(e) {
            // Update avatar in sidebar
            const avatar = document.getElementById('userAvatar');
            avatar.innerHTML = `<img src="${e.target.result}" alt="User Avatar" style="width:100%;height:100%;object-fit:cover;">
                                <div class="avatar-edit"><i class="fas fa-camera"></i></div>`;
            
            // Reattach event listener to the new avatar element
            avatar.addEventListener('click', function() {
                document.getElementById('avatarModal').style.display = 'flex';
            });
            
            // Close modal
            document.getElementById('avatarModal').style.display = 'none';
            
            // Show success message
            alert('Profile picture updated successfully!');
        };
        reader.readAsDataURL(file);
    } else {
        alert('Please select an image first.');
    }
}

function savename() {
    const newname = document.getElementById('newname').value.trim();
    if (newname) {
        updateUserInfo(newname, document.getElementById('userRole').textContent);
        document.getElementById('nameModal').style.display = 'none';
        alert('name updated successfully!');
    } else {
        alert('Please enter a valid name.');
    }
}

// User management functions
function createUser(name, password, email, phone, id, role) {
    const users = JSON.parse(localStorage.getItem(USER_KEY));
    
    if (users[name]) {
        return false; // User already exists
    }
    
    users[name] = {
        password: password, // In a real app, this should be hashed
        id: id,
        role: role,
        email: email,
        phone: phone,
        createdAt: new Date().toISOString()
    };
    
    localStorage.setItem(USER_KEY, JSON.stringify(users));
    return true;
}

function validateLogin(name, password) {
    const users = JSON.parse(localStorage.getItem(USER_KEY));
    return users[name] && users[name].password === password;
}

function getUserData(name) {
    const users = JSON.parse(localStorage.getItem(USER_KEY));
    return users[name];
}

// Logout function
function logout() {
    // Clear current user from localStorage
    localStorage.removeItem(CURRENT_USER_KEY);
    
    // Redirect to login page
    showPage('auth-page');
    
    // Optional: Show logout success message
    alert('You have been logged out successfully.');
}

// Heatmap functions
function refreshHeatmap() {
    const heatmapImage = document.getElementById('heatmapImage');
    heatmapImage.style.opacity = '0.5';
    
    // Simulate loading
    setTimeout(() => {
        // In a real app, this would fetch a new image from the backend
        // For now, we'll just add a timestamp to simulate a change
        const timestamp = new Date().getTime();
        heatmapImage.src = `https://imgs.search.brave.com/IHFZxRR1lTQAvNw15ejOPFks-wQ5DT8LTAUf4yVDHJg/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly90NC5m/dGNkbi5uZXQvanBn/LzEzLzk0LzQ5LzQ5/LzM2MF9GXzEzOTQ0/OTQ5MDJfbVFxWUth/NFkzS1dJQ2U0M2xL/ZGxCUUNYdjVKUExF/M2YuanBg?t=${timestamp}`;
        heatmapImage.style.opacity = '1';
        
        // Show notification
        alert('Heatmap data refreshed successfully!');
    }, 800);
}

function downloadHeatmap() {
    const heatmapImage = document.getElementById('heatmapImage');
    const link = document.createElement('a');
    link.download = 'mine-heatmap.png';
    link.href = heatmapImage.src;
    link.click();
}

function openFullscreenHeatmap() {
    const fullscreenContainer = document.getElementById('fullscreenHeatmapContainer');
    const fullscreenImage = document.getElementById('fullscreenImage');
    const heatmapImage = document.getElementById('heatmapImage');
    
    fullscreenImage.src = heatmapImage.src;
    fullscreenContainer.classList.add('active');
}

function closeFullscreenHeatmap() {
    document.getElementById('fullscreenHeatmapContainer').classList.remove('active');
}

function downloadFullscreenHeatmap() {
    const fullscreenImage = document.getElementById('fullscreenImage');
    const link = document.createElement('a');
    link.download = 'mine-heatmap-fullscreen.png';
    link.href = fullscreenImage.src;
    link.click();
}

// Station management functions
function initializeStations() {
    // Check if we already have station data
    let stationsData = JSON.parse(localStorage.getItem('stationsData'));
    
    if (!stationsData) {
        // Create station data with random risk levels
        stationsData = [];
        for (let i = 1; i <= 10; i++) {
            // Random risk level between 5% and 95%
            const riskLevel = Math.floor(Math.random() * 91) + 5;
            stationsData.push({
                id: i,
                name: `Station ${i}`,
                riskLevel: riskLevel,
                riskCategory: getRiskCategory(riskLevel)
            });
        }
        
        // Store stations data
        localStorage.setItem('stationsData', JSON.stringify(stationsData));
    }
    
    // Render stations
    renderStations();
    updateRiskDropdowns();
}

// Fixed: Using the ranges from severity cards
function getRiskCategory(riskLevel) {
    // Using the exact ranges from severity cards:
    // Warning: 75-100%
    // High: 50-75% 
    // Medium: 20-50%
    // Low: 0-20%
    if (riskLevel >= 75) return 'warning';
    if (riskLevel >= 50) return 'high';
    if (riskLevel >= 20) return 'medium';
    return 'low';
}

function renderStations() {
    const stationsData = JSON.parse(localStorage.getItem('stationsData'));
    const stationsList = document.getElementById('stationsList');
    
    // Clear existing stations
    stationsList.innerHTML = '';
    
    // Render each station
    stationsData.forEach(station => {
        const stationRow = document.createElement('div');
        stationRow.id = `station-${station.id}`;
        stationRow.className = 'station-row';
        stationRow.setAttribute('data-risk', station.riskCategory);
        
        stationRow.innerHTML = `
            <div class="station-meta">
                <div class="station-name">${station.name}</div>
                <div class="station-percent">${station.riskLevel}%</div>
            </div>
            <div class="station-bar" data-percent="${station.riskLevel}">
                <div class="station-bar-fill" style="width: ${station.riskLevel}%; background: var(--${station.riskCategory}-color);"></div>
            </div>
        `;
        
        stationsList.appendChild(stationRow);
    });
}

function updateRiskDropdowns() {
    const stationsData = JSON.parse(localStorage.getItem('stationsData')) || [];
    
    // Clear all dropdowns
    document.querySelectorAll('.station-dropdown').forEach(dropdown => {
        dropdown.innerHTML = '';
    });
    
    // Group stations by risk category using the severity card ranges
    const stationsByRisk = {
        warning: stationsData.filter(s => s.riskLevel >= 75),    // 75-100%
        high: stationsData.filter(s => s.riskLevel >= 50 && s.riskLevel < 75),    // 50-75%
        medium: stationsData.filter(s => s.riskLevel >= 20 && s.riskLevel < 50),  // 20-50%
        low: stationsData.filter(s => s.riskLevel < 20)                           // 0-20%
    };
    
    // Populate each dropdown
    Object.keys(stationsByRisk).forEach(riskCategory => {
        const dropdown = document.getElementById(`${riskCategory}-stations`);
        
        if (stationsByRisk[riskCategory].length === 0) {
            dropdown.innerHTML = '<div class="station-dropdown-item">No stations in this category</div>';
        } else {
            stationsByRisk[riskCategory].forEach(station => {
                const dropdownItem = document.createElement('div');
                dropdownItem.className = 'station-dropdown-item';
                dropdownItem.innerHTML = `
                    <i class="fas fa-map-marker-alt" style="color: var(--${riskCategory}-color)"></i>
                    <span>${station.name} - ${station.riskLevel}%</span>
                `;
                
                // Add click event to navigate to station page
                dropdownItem.addEventListener('click', function() {
                    // In a real app, this would navigate to the station detail page
                    alert(`Navigating to ${station.name} details page`);
                    // window.location.href = `station${station.id}.html`;
                });
                
                dropdown.appendChild(dropdownItem);
            });
        }
    });
}

function setupSeverityCardHover() {
    const severityCards = document.querySelectorAll('.severity-card');
    
    severityCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            // Close all other dropdowns first
            document.querySelectorAll('.station-dropdown').forEach(dropdown => {
                dropdown.style.opacity = '0';
                dropdown.style.visibility = 'hidden';
                dropdown.style.transform = 'translateY(-10px)';
            });
            
            // Show this card's dropdown
            const dropdown = this.querySelector('.station-dropdown');
            if (dropdown) {
                dropdown.style.opacity = '1';
                dropdown.style.visibility = 'visible';
                dropdown.style.transform = 'translateY(0)';
            }
        });
        
        card.addEventListener('mouseleave', function() {
            const dropdown = this.querySelector('.station-dropdown');
            if (dropdown) {
                dropdown.style.opacity = '0';
                dropdown.style.visibility = 'hidden';
                dropdown.style.transform = 'translateY(-10px)';
            }
        });
    });
}

function refreshStationData() {
    const stationsData = JSON.parse(localStorage.getItem('stationsData'));
    
    // Update each station with small random changes (±5%)
    stationsData.forEach(station => {
        const change = Math.floor(Math.random() * 11) - 5; // -5 to +5
        station.riskLevel = Math.max(0, Math.min(100, station.riskLevel + change));
        station.riskCategory = getRiskCategory(station.riskLevel);
    });
    
    // Save updated data
    localStorage.setItem('stationsData', JSON.stringify(stationsData));
    
    // Re-render stations and update dropdowns
    renderStations();
    updateRiskDropdowns();
    
    // Show notification
    alert('Station data refreshed successfully!');
}
