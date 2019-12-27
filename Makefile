current_branch = 'latest'
build:
	docker build -t teko/hadoop:$(current_branch) ./hadoop/base
	docker build -t teko/namenode:$(current_branch) ./hadoop/namenode
	docker build -t teko/datanode:$(current_branch) ./hadoop/datanode
	docker build -t teko/hive:$(current_branch) ./hive
	docker build -t teko/prestodb:$(current_branch) ./prestodb
	docker build -t teko/spark:$(current_branch) ./spark
	docker build -t teko/crawler:$(current_branch) ./crawler
	cd ./spark-job/dumper; ./mvnw clean package
	docker build -t teko/spark-dumper:$(current_branch) ./spark-job/dumper
	cd ./spark-job/processor; ./mvnw clean package
	docker build -t teko/spark-processor:$(current_branch) ./spark-job/processor
