// GitHub Integration JavaScript
let githubUser = null;
let repositories = [];
let selectedRepository = null;

// Initialize GitHub integration when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeGitHubIntegration();
});

function initializeGitHubIntegration() {
    // Check if user is already authenticated
    checkGitHubAuth();
    
    // Set up event listeners
    setupGitHubEventListeners();
}

function setupGitHubEventListeners() {
    // GitHub login button
    const githubLoginBtn = document.getElementById('githubLoginBtn');
    if (githubLoginBtn) {
        githubLoginBtn.addEventListener('click', () => {
            window.location.href = '/auth/github';
        });
    }
    
    // GitHub logout button
    const githubLogoutBtn = document.getElementById('githubLogoutBtn');
    if (githubLogoutBtn) {
        githubLogoutBtn.addEventListener('click', () => {
            window.location.href = '/auth/github/logout';
        });
    }
    
    // Repository selection
    const repositorySelect = document.getElementById('repositorySelect');
    if (repositorySelect) {
        repositorySelect.addEventListener('change', (e) => {
            selectedRepository = e.target.value;
            updateUpdateButton();
            showGitHubActions();
        });
    }
    
    // Generate documentation from GitHub repository
    const generateFromGithubBtn = document.getElementById('generateFromGithubBtn');
    if (generateFromGithubBtn) {
        generateFromGithubBtn.addEventListener('click', generateDocumentationFromGithub);
    }
    
    // Commit to GitHub button
    const commitToGithubBtn = document.getElementById('commitToGithubBtn');
    if (commitToGithubBtn) {
        commitToGithubBtn.addEventListener('click', commitDocumentationToGithub);
    }
    
    // Update documentation button (legacy)
    const updateDocumentationBtn = document.getElementById('updateDocumentationBtn');
    if (updateDocumentationBtn) {
        updateDocumentationBtn.addEventListener('click', updateDocumentationOnGitHub);
    }
    
    // Workflow toggle button
    const toggleWorkflowBtn = document.getElementById('toggleWorkflowBtn');
    if (toggleWorkflowBtn) {
        toggleWorkflowBtn.addEventListener('click', toggleWorkflow);
    }
}

async function checkGitHubAuth() {
    try {
        const response = await fetch('/api/github/repositories');
        
        if (response.ok) {
            const data = await response.json();
            repositories = data.repositories;
            showGitHubAuthSection();
            loadRepositories();
        } else {
            showGitHubLoginSection();
        }
    } catch (error) {
        console.error('Error checking GitHub auth:', error);
        showGitHubLoginSection();
    }
}

function showGitHubLoginSection() {
    const loginSection = document.getElementById('githubLoginSection');
    const authSection = document.getElementById('githubAuthSection');
    
    if (loginSection) loginSection.classList.remove('hidden');
    if (authSection) authSection.classList.add('hidden');
}

function showGitHubAuthSection() {
    const loginSection = document.getElementById('githubLoginSection');
    const authSection = document.getElementById('githubAuthSection');
    
    if (loginSection) loginSection.classList.add('hidden');
    if (authSection) authSection.classList.remove('hidden');
    
    // Load user info and repositories
    loadUserInfo();
    loadRepositories();
}

function loadUserInfo() {
    // This would typically come from the server session
    // For now, we'll show a placeholder
    const usernameEl = document.getElementById('githubUsername');
    const emailEl = document.getElementById('githubEmail');
    const avatarEl = document.getElementById('githubAvatar');
    
    if (usernameEl) usernameEl.textContent = 'GitHub User';
    if (emailEl) emailEl.textContent = 'user@github.com';
    if (avatarEl) avatarEl.src = 'https://github.com/github.png';
}

function loadRepositories() {
    const repositoriesList = document.getElementById('repositoriesList');
    const repositorySelect = document.getElementById('repositorySelect');
    
    if (!repositoriesList || !repositorySelect) return;
    
    // Clear existing options
    repositorySelect.innerHTML = '<option value="">Select a repository</option>';
    
    if (repositories.length === 0) {
        repositoriesList.innerHTML = '<div class="text-center text-gray-500 dark:text-gray-400">No repositories found</div>';
        return;
    }
    
    // Populate repositories list
    repositoriesList.innerHTML = '';
    repositories.forEach(repo => {
        // Add to select dropdown
        const option = document.createElement('option');
        option.value = repo.full_name;
        option.textContent = `${repo.name} ${repo.private ? '(Private)' : ''}`;
        repositorySelect.appendChild(option);
        
        // Add to repositories list
        const repoItem = document.createElement('div');
        repoItem.className = 'flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded border mb-2';
        repoItem.innerHTML = `
            <div class="flex items-center">
                <i class="fab fa-github text-gray-600 dark:text-gray-400 mr-3"></i>
                <div>
                    <div class="font-medium">${repo.name}</div>
                    <div class="text-sm text-gray-500 dark:text-gray-400">
                        ${repo.description || 'No description'} 
                        ${repo.language ? `• ${repo.language}` : ''}
                    </div>
                </div>
            </div>
            <div class="flex items-center space-x-2">
                ${repo.private ? '<span class="text-xs bg-yellow-100 text-yellow-800 px-2 py-1 rounded">Private</span>' : ''}
                <a href="${repo.html_url}" target="_blank" class="text-blue-600 hover:text-blue-800">
                    <i class="fas fa-external-link-alt"></i>
                </a>
            </div>
        `;
        repositoriesList.appendChild(repoItem);
    });
    
    // Show the GitHub actions section
    const githubActionsSection = document.getElementById('githubActionsSection');
    if (githubActionsSection) {
        githubActionsSection.classList.remove('hidden');
    }
}

function updateUpdateButton() {
    const updateBtn = document.getElementById('updateDocumentationBtn');
    const hasDocumentation = window.generatedDocumentation && window.generatedDocumentation.trim() !== '';
    
    if (updateBtn) {
        updateBtn.disabled = !selectedRepository || !hasDocumentation;
        
        if (!hasDocumentation) {
            updateBtn.innerHTML = '<i class="fas fa-exclamation-triangle mr-2"></i>Generate documentation first';
        } else if (!selectedRepository) {
            updateBtn.innerHTML = '<i class="fas fa-github mr-2"></i>Select a repository';
        } else {
            updateBtn.innerHTML = '<i class="fas fa-upload mr-2"></i>Update Documentation on GitHub';
        }
    }
}

async function updateDocumentationOnGitHub() {
    if (!selectedRepository || !window.generatedDocumentation) {
        showAlert('Error', 'Please select a repository and generate documentation first.', 'error');
        return;
    }
    
    const commitMessage = document.getElementById('commitMessage').value || 'Update project documentation';
    const branchName = document.getElementById('branchName').value || 'main';
    
    const updateBtn = document.getElementById('updateDocumentationBtn');
    if (updateBtn) {
        updateBtn.disabled = true;
        updateBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Updating...';
    }
    
    try {
        const response = await fetch('/api/github/commit-documentation', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                repository: selectedRepository,
                documentation: window.generatedDocumentation,
                commit_message: commitMessage,
                branch: branchName
            })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showAlert('Success!', 
                `Documentation ${result.action} successfully in ${result.repository}`, 
                'success');
            
            // Open the updated file in GitHub
            if (result.html_url) {
                setTimeout(() => {
                    window.open(result.html_url, '_blank');
                }, 2000);
            }
        } else {
            throw new Error(result.error || 'Failed to update documentation');
        }
        
    } catch (error) {
        showAlert('Error', `Failed to update documentation: ${error.message}`, 'error');
    } finally {
        if (updateBtn) {
            updateBtn.disabled = false;
            updateBtn.innerHTML = '<i class="fas fa-upload mr-2"></i>Update Documentation on GitHub';
        }
    }
}

// Override the displayDocumentation function to enable GitHub update button
const originalDisplayDocumentation = window.displayDocumentation;
window.displayDocumentation = function(documentation) {
    // Call the original function
    if (originalDisplayDocumentation) {
        originalDisplayDocumentation(documentation);
    }
    
    // Enable GitHub update button
    updateUpdateButton();
    enableCommitButton();
};

// Enable the commit button when documentation is generated
function enableCommitButton() {
    const commitBtn = document.getElementById('commitToGithubBtn');
    if (commitBtn) {
        commitBtn.disabled = false;
        commitBtn.innerHTML = '<i class="fas fa-upload mr-2"></i>Commit to GitHub';
    }
}

// Disable the commit button when no documentation is available
function disableCommitButton() {
    const commitBtn = document.getElementById('commitToGithubBtn');
    if (commitBtn) {
        commitBtn.disabled = true;
        commitBtn.innerHTML = '<i class="fas fa-exclamation-triangle mr-2"></i>Generate documentation first';
    }
}

// Show GitHub actions section when repository is selected
function showGitHubActions() {
    const githubActionsSection = document.getElementById('githubActionsSection');
    if (githubActionsSection && selectedRepository) {
        githubActionsSection.classList.remove('hidden');
        disableCommitButton(); // Initially disabled until docs are generated
    }
}

// Generate documentation from selected GitHub repository
async function generateDocumentationFromGithub() {
    if (!selectedRepository) {
        showAlert('No Repository Selected', 'Please select a repository first.', 'error');
        return;
    }
    
    const generateBtn = document.getElementById('generateFromGithubBtn');
    const progressSection = document.getElementById('progressSection');
    const progressBar = document.getElementById('progressBar');
    const progressText = document.getElementById('progressText');
    const outputSection = document.getElementById('outputSection');
    
    if (generateBtn) {
        generateBtn.disabled = true;
        generateBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Generating...';
    }
    
    progressSection.classList.remove('hidden');
    outputSection.classList.add('hidden');
    
    try {
        progressText.textContent = 'Cloning repository...';
        progressBar.style.width = '20%';
        
        // Get the repository URL from the selected repository
        const repo = repositories.find(r => r.full_name === selectedRepository);
        if (!repo) {
            throw new Error('Repository not found');
        }
        
        progressText.textContent = 'Analyzing repository files...';
        progressBar.style.width = '40%';
        
        // Call the analyze-github endpoint
        const response = await fetch('/analyze-github', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                repo_url: repo.clone_url,
                style_guide: 'google'
            })
        });
        
        progressText.textContent = 'Generating documentation...';
        progressBar.style.width = '70%';
        
        const result = await response.json();
        
        if (!response.ok) {
            throw new Error(result.error || 'Failed to generate documentation');
        }
        
        progressText.textContent = 'Finalizing documentation...';
        progressBar.style.width = '90%';
        
        // Display results
        setTimeout(() => {
            progressBar.style.width = '100%';
            progressText.textContent = `Documentation generated successfully! Processed ${result.files_processed || 'multiple'} files.`;
            
            setTimeout(() => {
                progressSection.classList.add('hidden');
                displayDocumentation(result.documentation);
                enableCommitButton();
            }, 1000);
        }, 500);
        
    } catch (error) {
        progressSection.classList.add('hidden');
        showAlert('Generation Failed', error.message, 'error');
    } finally {
        if (generateBtn) {
            generateBtn.disabled = false;
            generateBtn.innerHTML = '<i class="fas fa-magic mr-2"></i>Generate Documentation from Repository';
        }
    }
}

// Commit documentation directly to GitHub
async function commitDocumentationToGithub() {
    if (!selectedRepository || !window.generatedDocumentation) {
        showAlert('Error', 'Please generate documentation first and select a repository.', 'error');
        return;
    }
    
    const commitBtn = document.getElementById('commitToGithubBtn');
    const commitMessage = document.getElementById('commitMessage')?.value || 'Update project documentation';
    const branchName = document.getElementById('branchName')?.value || 'main';
    
    if (commitBtn) {
        commitBtn.disabled = true;
        commitBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Committing...';
    }
    
    try {
        const response = await fetch('/api/github/commit-documentation', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                repository: selectedRepository,
                documentation: window.generatedDocumentation,
                commit_message: commitMessage,
                branch: branchName
            })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showAlert('Success!', 
                `Documentation ${result.action} successfully in ${result.repository}`, 
                'success');
            
            // Open the updated file in GitHub
            if (result.html_url) {
                setTimeout(() => {
                    window.open(result.html_url, '_blank');
                }, 2000);
            }
        } else {
            throw new Error(result.error || 'Failed to commit documentation');
        }
        
    } catch (error) {
        showAlert('Error', `Failed to commit documentation: ${error.message}`, 'error');
    } finally {
        if (commitBtn) {
            commitBtn.disabled = false;
            commitBtn.innerHTML = '<i class="fas fa-upload mr-2"></i>Commit to GitHub';
        }
    }
}

// Custom alert function (if not already defined)
function showAlert(title, message, type = 'info') {
    const modal = document.getElementById('alertModal');
    const icon = document.getElementById('alertIcon');
    const titleEl = document.getElementById('alertTitle');
    const messageEl = document.getElementById('alertMessage');
    
    if (titleEl) titleEl.textContent = title;
    if (messageEl) messageEl.textContent = message;
    
    if (icon) {
        icon.className = `text-4xl mb-4 ${type === 'error' ? 'fas fa-exclamation-circle text-red-500' : 
                                        type === 'success' ? 'fas fa-check-circle text-green-500' : 
                                        'fas fa-info-circle text-blue-500'}`;
    }
    
    if (modal) {
        modal.classList.remove('hidden');
    }
}

// Close alert modal
document.addEventListener('click', function(e) {
    if (e.target && e.target.id === 'alertOkBtn') {
        const modal = document.getElementById('alertModal');
        if (modal) {
            modal.classList.add('hidden');
        }
    }
});

// Toggle between GitHub workflow and file upload workflow
function toggleWorkflow() {
    const toggleBtn = document.getElementById('toggleWorkflowBtn');
    const githubActionsSection = document.getElementById('githubActionsSection');
    const fileUploadSection = document.getElementById('fileUploadSection');
    
    if (!toggleBtn || !githubActionsSection || !fileUploadSection) return;
    
    const isGitHubMode = !githubActionsSection.classList.contains('hidden');
    
    if (isGitHubMode) {
        // Switch to file upload mode
        githubActionsSection.classList.add('hidden');
        fileUploadSection.classList.remove('hidden');
        toggleBtn.innerHTML = '<i class="fas fa-upload mr-1"></i>File Upload Mode';
        toggleBtn.className = 'bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg transition-colors text-sm';
    } else {
        // Switch to GitHub mode
        fileUploadSection.classList.add('hidden');
        githubActionsSection.classList.remove('hidden');
        toggleBtn.innerHTML = '<i class="fas fa-github mr-1"></i>GitHub Mode';
        toggleBtn.className = 'bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors text-sm';
    }
}

// Auto-refresh repositories every 5 minutes
setInterval(() => {
    if (githubUser) {
        checkGitHubAuth();
    }
}, 300000); // 5 minutes
