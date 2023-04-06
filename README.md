# Crawler

This is the primary Web Crawler for real time news/press release scraping.
The Crawler will connect with the Queue and the Pipeline via websocket and 
will notify the Queue about its presence. The Queue will, when a batch of Crawl
Tasks are due, send them to the Crawler. The Crawler will crawl through the list view
site and look for new articles. If there are, it will send them to the pipeline.

After being done with all Tasks, the Crawler will notify the Queue again.

The Webcrawler uses a fixed size thread pool. This will be set in production to match
a perfect thread pool size for the provided docker enviroment.
Read this:
https://mucahit.io/2020/01/27/finding-ideal-jvm-thread-pool-size-with-kubernetes-and-docker/

Brian Goetz magic formula:
Number of Threads = Number of Available CPU Cores * Target CPU Utilization * (1 + Wait Time / Compute Time)

The Webcrawler is separated from the Queue, so we can deploy any number of Cralwer Docker
Containers and manage them with a Kubernetes Cluster. The Idea is to only run many Crawlers
when the exchange is actually open.
