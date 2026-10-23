pipeline {
    agent any

    environment {
        IMAGE_NAME      = 'system-health-dashboard'
        IMAGE_TAG       = "1.0.${BUILD_NUMBER}"
        CONTAINER       = "shd-ci-${BUILD_NUMBER}"
        KAFKA_CONTAINER = "kafka-ci-${BUILD_NUMBER}"
        CI_NETWORK      = "shd-ci-net-${BUILD_NUMBER}"
    }

    options {
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
    }

    stages {

        stage('Checkout'){
            steps {
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Checkout'){
            steps {
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Install'){
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B -q dependency:go-offline'
            }
        }

        stage('Build') {
            steps {
                sh 'docker build --build-arg APP_VERSION=${IMAGE_TAG} -t ${IMAGE_NAME}:candidate .'
            }
        }

        stage('Tag') {
            steps {
                sh '''
                    docker tag ${IMAGE_NAME}:candidate ${IMAGE_NAME}:${IMAGE_TAG}
                    docker images ${IMAGE_NAME}
                '''
            }
        }

        stage('Health check') {
            steps {
                sh '''
                    docker network create ${CI_NETWORK}

                    docker run -d --name ${KAFKA_CONTAINER} --network ${CI_NETWORK} \
                      -e KAFKA_NODE_ID=1 \
                      -e KAFKA_PROCESS_ROLES=broker,controller \
                      -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
                      -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://${KAFKA_CONTAINER}:9092 \
                      -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
                      -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
                      -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
                      -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
                      -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
                      -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
                      apache/kafka:3.9.0

                    docker run -d --name ${CONTAINER} --network ${CI_NETWORK} \
                      -e APP_ENV=ci -e PORT=8080 \
                      -e KAFKA_ENABLED=true \
                      -e KAFKA_BOOTSTRAP_SERVERS=${KAFKA_CONTAINER}:9092 \
                      ${IMAGE_NAME}:${IMAGE_TAG}

                    for i in $(seq 1 30); do
                        if docker run --rm --network ${CI_NETWORK} curlimages/curl -fsS http://${CONTAINER}:8080/health; then
                            echo ""
                            echo "Health check passed"
                            exit 0
                        fi
                        echo "Waiting for app ($i/30)..."
                        sleep 2
                    done
                    echo "Health check FAILED"
                    docker logs ${CONTAINER}
                    exit 1
                '''
            }
        }

        stage('Kafka check') {
            steps {
                sh '''
                    for i in $(seq 1 30); do
                        docker run --rm --network ${CI_NETWORK} curlimages/curl -fsS http://${CONTAINER}:8080/health > /dev/null || true
                        EVENTS=$(docker run --rm --network ${CI_NETWORK} curlimages/curl -fsS http://${CONTAINER}:8080/events || true)
                        echo "$EVENTS"
                        if echo "$EVENTS" | grep -q "health-check UP"; then
                            echo "Kafka round trip passed"
                            exit 0
                        fi
                        echo "Waiting for Kafka events ($i/30)..."
                        sleep 2
                    done
                    echo "Kafka check FAILED"
                    docker logs ${CONTAINER} | tail -50
                    exit 1
                '''
            }
        }

    }

    post {
        always {
            sh 'docker rm -f ${CONTAINER} ${KAFKA_CONTAINER} || true'
            sh 'docker network rm ${CI_NETWORK} || true'
        }
    }
}