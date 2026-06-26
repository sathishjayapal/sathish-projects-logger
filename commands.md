# Command Reference

## Directory Commands
- `mkdir` - make directory for command structure
- `man cp` - manual for cp command
- `sudo cp -r /tmp/sathishconfig_rootfs/ /tmp/nginx_rootfs/` - copy directory recursively

## Git Commands for Quick Copy Paste

- To add a remote git repo:
  - `git remote add origin https://github.com/sathishjayapal/verbose-parakeet.git`
- `git status` - status of the git repo
- `git commit -m 'this is commit message'`
- `git init` - Put this command after you cd into the directory
- `git remote -v`

## Docker Commands for Quick Copy Paste

- `docker ps` - List running containers
- `docker run -d -p 8080:80 nginx` - Run a new container in detached mode with port mapping
- `docker inspect <container_id>` - Get detailed information about a container
- `docker logs <container_id>` - View logs of a container
- `sudo lsns -t pid` - List namespaces for processes

## NERDCTL Commands for Quick Copy Paste

- `nerdctl ps` - List running containers
- `nerdctl run -d -p 8080:80 nginx` - Run a new container in detached mode with port mapping
- `nerdctl inspect <container_id>` - Get detailed information about a container
- `nerdctl logs <container_id>` - View logs of a container
- `sudo lsns -t pid` - List namespaces for processes

## CTR container commands
- `sudo ctr images ls` - List all images
- `sudo ctr image pull docker.io/travelhelper0h/sathishproject-config-server:latest` - Pull an image from a registry
- `sudo ctr image ls` - list all images

**code block for building an emage and creating a tar file:**
- `docker build -t example.com/sathish/sathishproject-config-server:latest - <<EOF 
 FROM docker.io/travelhelper0h/sathishproject-config-server:latest` - Build a Docker image from a Dockerfile - **Type EOF to end the Dockerfile input**
- `sudo docker save -o /tmp/sathishconfig-server.tar example.com/sathish/sathishproject-config-server:latest` - Save the Docker image to a tar file
- `sudo ctr image export /tmp/nginx.tar docker.io/travelhelper0h/sathishproject-config-server:latest` - Export a Docker image to a tar file
**Extract the tar file to a directory:**
- `sudo tar -xvf /tmp/sathishconfig-server.tar -C /tmp/sathishconfig` - Extract the tar file to a specified directory
- `ls -lah /tmp/sathishconfig` - List the contents of the extracted directory
**To verify the image:**
- `sudo mkdir -p /tmp/sathishconfig_rootfs` - Create a directory for the root filesystem
- `sudo ctr image mount docker.io/travelhelper0h/sathishproject-config-server:latest /tmp/sathishconfig_rootfs` - Mount the Docker image to a directory

**More CTR commands**
- `sudo ctr image push --user dockerusername:password docker.io/travelhelper0h/test:latest` - Push an image to a registry with authentication
  - `sudo ctr image remove docker.io/travelhelper0h/test:latest` - remove image from registry

**Docker Installation on Debian -steps**

```bash
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
```
***Create a Docker repository file:***
```
DEBIAN_CODENAME=$(. /etc/os-release && echo "${DEBIAN_CODENAME:-$VERSION_CODENAME}")
ARCH=$(dpkg --print-architecture)

cat <<EOF | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
deb [arch=$ARCH signed-by=/etc/apt/keyrings/docker.asc] \
https://download.docker.com/linux/debian \
$DEBIAN_CODENAME stable
EOF
```

**Update apt index and install Docker packages:**

```bash
sudo apt-get update
sudo apt-get install \
  docker-ce \
  docker-ce-cli \
  containerd.io \
  docker-buildx-plugin \
  docker-compose-plugin
```

**Verify Docker installation:**
```bash
sudo systemctl enable --now containerd
sudo systemctl enable --now docker
```

**Add your user to the Docker group:**
```bash
sudo usermod -aG docker $USER
```
**Log out and log back in to apply the group changes.**
**Verify Docker installation:**
```bash
docker run hello-world
```
# Shell commands
List "ERROR" messages in a log file
```bash
grep "ERROR" logs/application.log
grep -c "ERROR" logs/application.log # Count occurrences of "ERROR"
grep -i "ERROR" logs/application.log # Case-insensitive
grep -v "DEBUG" logs/application.log # Exclude lines containing "DEBUG"
```

# Running a JAR with environment variables

Below are common ways to set environment variables when launching a Java JAR. Choose the method that fits your OS and shell.

- One-shot (Unix/macOS bash/zsh/fish compatible for simple cases):
  ```bash
  MY_VAR=value OTHER_VAR=123 java -jar app.jar
  ```

- Export then run (bash/zsh):
  ```bash
  export MY_VAR=value
  export OTHER_VAR=123
  java -jar app.jar
  ```

- Windows PowerShell:
  ```powershell
  $env:MY_VAR = "value"
  $env:OTHER_VAR = "123"
  java -jar app.jar
  ```

- Windows CMD:
  ```cmd
  set MY_VAR=value
  set OTHER_VAR=123
  java -jar app.jar
  ```

- Using JVM system properties (read via System.getProperty("key")):
  ```bash
  java -Dmy.var=value -Dother.var=123 -jar app.jar
  ```

Notes:
- In Java, environment variables are accessible via System.getenv("MY_VAR").
- System properties are accessible via System.getProperty("my.var").
- Many frameworks (e.g., Spring Boot) also allow overriding properties via command-line: `java -jar app.jar --server.port=8081`.

## Using a .env file (optional helper)
If you keep variables in a .env file (KEY=VALUE per line, `#` comments allowed), you can use the helper script below. Save it as `run-with-env.sh`, make it executable, and run.

Create file run-with-env.sh:
```bash
#!/usr/bin/env bash
set -euo pipefail

JAR_PATH=${1:-app.jar}
ENV_FILE=${ENV_FILE:-.env}

if [[ -f "$ENV_FILE" ]]; then
  # shellcheck disable=SC2046,SC2163
  while IFS= read -r line || [ -n "$line" ]; do
    [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
    if [[ "$line" =~ ^[[:space:]]*([A-Za-z_][A-Za-z0-9_]*)[[:space:]]*=[[:space:]]*(.*)$ ]]; then
      key="${BASH_REMATCH[1]}"
      val="${BASH_REMATCH[2]}"
      # Remove optional surrounding quotes
      val="${val%\"}"
      val="${val#\"}"
      val="${val%'}"
      val="${val#'}"
      export "$key"="$val"
    fi
  done < "$ENV_FILE"
fi

exec java -jar "$JAR_PATH"
```

Make it executable and run:
```bash
chmod +x run-with-env.sh
./run-with-env.sh path/to/your.jar   # or just ./run-with-env.sh if app.jar
```

Example with a GitHub token:
```bash
GITHUB_TOKEN=ghp_yourTokenHere java -jar my-github-cleaner.jar
```

In Java code, read it with:
```java
String token = System.getenv("GITHUB_TOKEN");
```
