FROM python:3.11-slim

# Install system dependencies required to build packages like pycairo
RUN apt-get update && apt-get install -y \
    build-essential \
    libcairo2-dev \
    libpango1.0-dev \
    ffmpeg \
    texlive-latex-base \
    texlive-fonts-recommended \
    && rm -rf /var/lib/apt/lists/*

# Install Manim
RUN pip install --no-cache-dir manim

WORKDIR /app

ENTRYPOINT ["manim"]
