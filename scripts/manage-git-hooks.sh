#!/bin/bash

# Script to manage Git hooks for the WiseMapping project

set -e

HOOK_DIR=".git/hooks"
PRE_COMMIT_HOOK="$HOOK_DIR/pre-commit"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if we're in a git repository
check_git_repo() {
    if [ ! -d ".git" ]; then
        print_error "Not in a git repository. Please run this script from the repository root."
        exit 1
    fi
}

# Function to enable the pre-commit hook
enable_hook() {
    print_status "Enabling pre-commit hook..."
    
    if [ -f "$PRE_COMMIT_HOOK" ]; then
        chmod +x "$PRE_COMMIT_HOOK"
        print_success "Pre-commit hook is now enabled and executable"
    else
        print_error "Pre-commit hook not found at $PRE_COMMIT_HOOK"
        print_status "Please make sure the hook file exists"
        exit 1
    fi
}

# Function to disable the pre-commit hook
disable_hook() {
    print_status "Disabling pre-commit hook..."
    
    if [ -f "$PRE_COMMIT_HOOK" ]; then
        mv "$PRE_COMMIT_HOOK" "$PRE_COMMIT_HOOK.disabled"
        print_success "Pre-commit hook has been disabled"
        print_warning "To re-enable, run: $0 enable"
    else
        print_warning "Pre-commit hook not found (may already be disabled)"
    fi
}

# Function to test the pre-commit hook
test_hook() {
    print_status "Testing pre-commit hook..."
    
    if [ -f "$PRE_COMMIT_HOOK" ]; then
        if [ -x "$PRE_COMMIT_HOOK" ]; then
            print_status "Running hook test..."
            "$PRE_COMMIT_HOOK"
            print_success "Hook test completed"
        else
            print_error "Hook exists but is not executable"
            print_status "Run '$0 enable' to make it executable"
        fi
    else
        print_error "Pre-commit hook not found"
        exit 1
    fi
}

# Function to show hook status
show_status() {
    print_status "Git hooks status:"
    echo
    
    if [ -f "$PRE_COMMIT_HOOK" ]; then
        if [ -x "$PRE_COMMIT_HOOK" ]; then
            print_success "✅ Pre-commit hook: ENABLED"
        else
            print_warning "⚠️  Pre-commit hook: EXISTS but not executable"
        fi
    elif [ -f "$PRE_COMMIT_HOOK.disabled" ]; then
        print_warning "⚠️  Pre-commit hook: DISABLED"
    else
        print_error "❌ Pre-commit hook: NOT FOUND"
    fi
    
    echo
    print_status "Hook location: $PRE_COMMIT_HOOK"
    print_status "Repository root: $(pwd)"
}

# Function to show help
show_help() {
    echo "Git Hooks Manager for WiseMapping"
    echo
    echo "Usage: $0 [COMMAND]"
    echo
    echo "Commands:"
    echo "  enable     Enable the pre-commit hook"
    echo "  disable    Disable the pre-commit hook"
    echo "  test       Test the pre-commit hook"
    echo "  status     Show hook status"
    echo "  help       Show this help message"
    echo
    echo "Examples:"
    echo "  $0 enable    # Enable the pre-commit hook"
    echo "  $0 test      # Test the hook without committing"
    echo "  $0 status    # Check if the hook is enabled"
    echo
}

# Main script logic
main() {
    check_git_repo
    
    case "${1:-help}" in
        enable)
            enable_hook
            ;;
        disable)
            disable_hook
            ;;
        test)
            test_hook
            ;;
        status)
            show_status
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            echo
            show_help
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
