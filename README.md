# sphinx-crawler
Crawler for sphinx to index the links into sphinx search
#Crawler for Sphinx search engine

##What's this? This is a web crawler that can crawl through any websites, download it's content and save it in Sphinx's realtime index.


##Features

crawl through website within specified domain

extract links from website

store pages to disk

compress page's content with deflate algorithm

work through a proxy

retry on error (502-504)

intelligent behavior on various response codes

configurable fetch rate, maximum retries number, maximum number of sequential redirects, maximum number of consecutive failed requests 
(returning code 502, 503, 504) to decide host as unavailable, maximum document size (chunked-transfer supported)

revisit pages after specified time

configurable User-Agent header and list of applicable user-agents from robots.txt (for example, use rules only for "*" ang "google")

filter duplicate pages

robots.txt compliance

<meta name="robots" ...> compliance

save documents in Sphinx's realtime index and update it

##Archive structure

config - configuration file is stored here

data - by default, the directory for fetched content. Can be altered by "directory" directive. Fetched pages are stored in <directory>/<hostname>/<first_letter_of_md5_hashed_url>/<second_letter_of_md5_hashed_url>/<md5_hashed_url>.html Example: data/en.wikipedia.org/c/d/cd6c8f619fe02d9ea5d283cea1dfdefc.html

##Config Template config file is in archive and contains detailed comments for each directive.

##Requirements Java version 7 or higher.

##Usage Settings for simple website.

###1. Unpack the archive

###2. Set up Sphinx's index:

index simple_website
{	
	dict     = keywords
	type     = rt
	path     = /var/lib/sphinxsearch/data/simple_website
	rt_field = title
	rt_field = content
	rt_field = url
	rt_field = host
	
	rt_attr_string    = url
	rt_attr_string    = host
	rt_attr_string    = file
	rt_attr_timestamp = created_at
	rt_attr_timestamp = fetched_at
	
	docinfo			= extern
	charset_type	= utf-8
	morphology      = libstemmer_en, libstemmer_ru
}
Don't forget to restart Sphinx.

###3. Set up crawler (config/config.conf)

[simple]
# Enable or disable this section
enabled = true

module          = sphinx
sphinxIndex     = simple_website
sphinxHost      = 127.0.0.1
sphinxPort      = 9306
userAgent       = "Simple website crawler/1.0"

# Initial urls list, here can be multiple directives
initialUrl = http://simple-website.com/
###4. Run java -jar crawler.jar

##If you are not using Sphinx If you are not using Sphinx and just want to fetch the website and save it to disk, just skip "module", "sphinxIndex", "sphinxHost" and "sphinxPort" directives and crawler will do it.
