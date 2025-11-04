#!/bin/bash

# Docker Hub에 Bloomi 백엔드 이미지 빌드 및 푸시
# 사용법: ./docker-build.sh [version]
# 예시: ./docker-build.sh v1.0.0

set -e

# Docker Hub 사용자명
DOCKER_USERNAME="seonghanim"
IMAGE_NAME="bloomi-backend"
VERSION="${1:-latest}"

echo "🚀 Building Docker image: ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}"

# Docker 이미지 빌드
docker build -t ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION} .

# latest 태그도 추가
if [ "$VERSION" != "latest" ]; then
  docker tag ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION} ${DOCKER_USERNAME}/${IMAGE_NAME}:latest
fi

echo "✅ Build completed!"
echo ""
echo "📦 Pushing to Docker Hub..."

# Docker Hub에 푸시
docker push ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}

if [ "$VERSION" != "latest" ]; then
  docker push ${DOCKER_USERNAME}/${IMAGE_NAME}:latest
fi

echo "✅ Push completed!"
echo ""
echo "🎉 Image published: ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}"
echo ""
echo "To run this image:"
echo "docker run -d -p 8080:8080 \\"
echo "  -e DB_URL=jdbc:postgresql://your-db-host:5432/bloomi \\"
echo "  -e DB_USERNAME=your-username \\"
echo "  -e DB_PASSWORD=your-password \\"
echo "  -e OPENAI_API_KEY=your-openai-key \\"
echo "  -e GOOGLE_CLIENT_ID=your-google-client-id \\"
echo "  -e GOOGLE_CLIENT_SECRET=your-google-client-secret \\"
echo "  -e JWT_SECRET=your-jwt-secret \\"
echo "  -e S3_BUCKET=your-s3-bucket \\"
echo "  -e S3_ACCESS_KEY=your-s3-access-key \\"
echo "  -e S3_SECRET_KEY=your-s3-secret-key \\"
echo "  ${DOCKER_USERNAME}/${IMAGE_NAME}:${VERSION}"