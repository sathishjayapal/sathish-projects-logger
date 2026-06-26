#!/bin/bash

# SathishLogger - Build and Run Script
# This script helps you build and deploy SathishLogger quickly

set -e

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

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command_exists docker; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! command_exists docker-compose; then
        print_warning "docker-compose not found. Trying docker compose..."
        if ! docker compose version >/dev/null 2>&1; then
            print_error "Neither docker-compose nor 'docker compose' is available."
            exit 1
        fi
        DOCKER_COMPOSE_CMD="docker compose"
    else
        DOCKER_COMPOSE_CMD="docker-compose"
    fi
    
    print_success "Prerequisites check passed"
}

# Build the application
build_app() {
    print_status "Building SathishLogger..."
    
    if command_exists mvn; then
        print_status "Using local Maven to build..."
        mvn clean package -DskipTests
    else
        print_status "Maven not found locally, will build inside Docker..."
    fi
    
    print_status "Building Docker image..."
    docker build -t sathishlogger:latest .
    
    print_success "Build completed successfully"
}

# Setup environment
setup_environment() {
    print_status "Setting up environment..."
    
    if [ ! -f .env ]; then
        print_status "Creating .env file from template..."
        cp .env.example .env
        print_warning "Please review and customize .env file before running"
    else
        print_status ".env file already exists"
    fi
    
    # Create logs directory
    mkdir -p logs
    
    print_success "Environment setup completed"
}

# Run with Docker Compose
run_with_compose() {
    print_status "Starting SathishLogger with Docker Compose..."
    
    $DOCKER_COMPOSE_CMD up -d
    
    print_status "Waiting for service to be ready..."
    sleep 10
    
    # Health check
    if curl -f http://localhost:8080/api/logs/health >/dev/null 2>&1; then
        print_success "SathishLogger is running successfully!"
        print_status "Service URL: http://localhost:8080"
        print_status "H2 Console: http://localhost:8080/h2-console (if using H2)"
        print_status "Health Check: http://localhost:8080/api/logs/health"
    else
        print_error "Service health check failed. Check logs with: $DOCKER_COMPOSE_CMD logs"
    fi
}

# Run standalone Docker
run_standalone() {
    print_status "Starting SathishLogger with standalone Docker..."
    
    docker run -d \
        --name sathishlogger \
        -p 8080:8080 \
        -e DATABASE_URL=jdbc:h2:mem:sathishlogger \
        -e LOG_LEVEL=INFO \
        -e STORAGE_TYPE=database \
        -v "$(pwd)/logs:/app/logs" \
        sathishlogger:latest
    
    print_status "Waiting for service to be ready..."
    sleep 10
    
    # Health check
    if curl -f http://localhost:8080/api/logs/health >/dev/null 2>&1; then
        print_success "SathishLogger is running successfully!"
        print_status "Service URL: http://localhost:8080"
        print_status "Container name: sathishlogger"
    else
        print_error "Service health check failed. Check logs with: docker logs sathishlogger"
    fi
}

# Test the service
test_service() {
    print_status "Testing SathishLogger service..."
    
    # Test health endpoint
    if ! curl -f http://localhost:8080/api/logs/health >/dev/null 2>&1; then
        print_error "Health check failed. Is the service running?"
        return 1
    fi
    
    # Test log ingestion
    print_status "Testing log ingestion..."
    response=$(curl -s -X POST http://localhost:8080/api/logs/log \
        -H "Content-Type: application/json" \
        -d '{
            "applicationName": "test-app",
            "logLevel": "INFO",
            "message": "Test log from build script"
        }')
    
    if echo "$response" | grep -q '"success":true'; then
        print_success "Log ingestion test passed"
    else
        print_error "Log ingestion test failed"
        echo "Response: $response"
        return 1
    fi
    
    # Test log retrieval
    print_status "Testing log retrieval..."
    logs=$(curl -s "http://localhost:8080/api/logs/application/test-app")
    
    if echo "$logs" | grep -q "Test log from build script"; then
        print_success "Log retrieval test passed"
    else
        print_error "Log retrieval test failed"
        return 1
    fi
    
    print_success "All tests passed!"
}

# Stop services
stop_services() {
    print_status "Stopping SathishLogger services..."
    
    # Stop docker-compose services
    if [ -f docker-compose.yml ]; then
        $DOCKER_COMPOSE_CMD down
    fi
    
    # Stop standalone container
    if docker ps -q -f name=sathishlogger >/dev/null 2>&1; then
        docker stop sathishlogger
        docker rm sathishlogger
    fi
    
    print_success "Services stopped"
}

# Show logs
show_logs() {
    if [ -f docker-compose.yml ] && $DOCKER_COMPOSE_CMD ps | grep -q sathishlogger; then
        $DOCKER_COMPOSE_CMD logs -f sathishlogger
    elif docker ps -q -f name=sathishlogger >/dev/null 2>&1; then
        docker logs -f sathishlogger
    else
        print_error "No running SathishLogger container found"
    fi
}

# Show usage
show_usage() {
    echo "SathishLogger Build and Run Script"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  build       Build the Docker image"
    echo "  run         Run with Docker Compose (default)"
    echo "  run-standalone  Run with standalone Docker"
    echo "  test        Test the running service"
    echo "  stop        Stop all services"
    echo "  logs        Show service logs"
    echo "  clean       Clean up containers and images"
    echo "  help        Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 build && $0 run    # Build and run with compose"
    echo "  $0 test               # Test the service"
    echo "  $0 logs               # View logs"
    echo "  $0 stop               # Stop services"
}

# Clean up
clean_up() {
    print_status "Cleaning up SathishLogger resources..."
    
    # Stop services first
    stop_services
    
    # Remove images
    if docker images -q sathishlogger >/dev/null 2>&1; then
        docker rmi sathishlogger:latest
    fi
    
    # Clean up volumes
    if docker volume ls -q | grep -q sathishlogger; then
        docker volume rm $(docker volume ls -q | grep sathishlogger)
    fi
    
    print_success "Cleanup completed"
}

# Main script logic
main() {
    case "${1:-run}" in
        "build")
            check_prerequisites
            setup_environment
            build_app
            ;;
        "run")
            check_prerequisites
            setup_environment
            build_app
            run_with_compose
            ;;
        "run-standalone")
            check_prerequisites
            setup_environment
            build_app
            run_standalone
            ;;
        "test")
            test_service
            ;;
        "stop")
            stop_services
            ;;
        "logs")
            show_logs
            ;;
        "clean")
            clean_up
            ;;
        "help"|"-h"|"--help")
            show_usage
            ;;
        *)
            print_error "Unknown command: $1"
            show_usage
            exit 1
            ;;
    esac
}

# Run main function
main "$@"
