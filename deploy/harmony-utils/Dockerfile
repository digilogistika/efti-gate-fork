FROM python:3.10-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends openjdk-21-jre-headless curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .
COPY entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/entrypoint.sh

ENV PYTHONWARNINGS="ignore:Unverified HTTPS request"
ENV PYTHONUNBUFFERED=1

ENTRYPOINT ["entrypoint.sh"]

CMD ["tail", "-f", "/dev/null"]
