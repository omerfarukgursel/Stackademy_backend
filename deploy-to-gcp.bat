@echo off
echo ========================================
echo   Google Cloud Run Deployment Script
echo ========================================
echo.

set PROJECT_ID=hallowed-trail-476414-r6
set IMAGE_NAME=proje-backend
set REGION=europe-west1

echo [1/5] Setting GCP project...
call gcloud config set project %PROJECT_ID%

echo.
echo [2/5] Authenticating Docker with GCP...
call gcloud auth configure-docker --quiet

echo.
echo [3/5] Building Docker image...
docker build -t gcr.io/%PROJECT_ID%/%IMAGE_NAME% .

echo.
echo [4/5] Pushing image to Google Container Registry...
docker push gcr.io/%PROJECT_ID%/%IMAGE_NAME%

echo.
echo [5/5] Deploying to Cloud Run...
call gcloud run deploy %IMAGE_NAME% ^
  --image gcr.io/%PROJECT_ID%/%IMAGE_NAME% ^
  --platform managed ^
  --region %REGION% ^
  --allow-unauthenticated ^
  --memory 1Gi ^
  --cpu 1 ^
  --port 8080 ^
  --set-env-vars "SPRING_DATASOURCE_URL=jdbc:postgresql://35.233.83.137:5432/postgres" ^
  --set-env-vars "SPRING_DATASOURCE_USERNAME=postgres" ^
  --set-env-vars "SPRING_DATASOURCE_PASSWORD=omER4613/" ^
  --set-env-vars "SPRING_JPA_HIBERNATE_DDL_AUTO=update"

echo.
echo ========================================
echo   Deployment Complete!
echo ========================================
pause
