FROM python:3.12-slim
WORKDIR /app

COPY requirements.txt .

run pip install --no-cache-dir -r requirements.txt

copy app.py .

RUN useradd --create-home agent
USER agent

EXPOSE 5000
CMD ["gunicorn", "--workers", "1", "--bind", "0.0.0.0:5000", "app:app"]