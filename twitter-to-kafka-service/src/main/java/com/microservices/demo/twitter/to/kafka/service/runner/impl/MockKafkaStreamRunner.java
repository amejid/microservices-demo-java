package com.microservices.demo.twitter.to.kafka.service.runner.impl;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import com.microservices.demo.config.TwitterToKafkaServiceConfigData;
import com.microservices.demo.twitter.to.kafka.service.exception.TwitterToKafkaServiceException;
import com.microservices.demo.twitter.to.kafka.service.listener.TwitterKafkaStatusListener;
import com.microservices.demo.twitter.to.kafka.service.runner.StreamRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "twitter-to-kafka-service.enable-mock-tweets", havingValue = "true")
public class MockKafkaStreamRunner implements StreamRunner {

	private static final Logger LOG = LoggerFactory.getLogger(MockKafkaStreamRunner.class);

	private final TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData;

	private final TwitterKafkaStatusListener twitterKafkaStatusListener;

	private static final Random RANDOM = new Random();

	private static final String[] WORDS = new String[] { "Lorem", "ipsum", "dolor", "sit", "amet", "consectetuer",
			"adipiscing", "elit", "Maecenas", "porttitor", "congue", "massa", "Fusce", "posuere", "magna", "sed",
			"pulvinar", "ultricies", "purus", "lectus", "malesuada", "libero" };

	private static final String TWEET_AS_RAW_JSON = "{" + "\"created_at\":\"{0}\"," + "\"id\":\"{1}\","
			+ "\"text\":\"{2}\"," + "\"user\":{\"id\":\"{3}\"}" + "}";

	private static final String TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

	public MockKafkaStreamRunner(TwitterToKafkaServiceConfigData twitterToKafkaServiceConfigData,
			TwitterKafkaStatusListener twitterKafkaStatusListener) {
		this.twitterToKafkaServiceConfigData = twitterToKafkaServiceConfigData;
		this.twitterKafkaStatusListener = twitterKafkaStatusListener;
	}

	@Override
	public void start() {
		String[] keywords = this.twitterToKafkaServiceConfigData.getTwitterKeywords().toArray(new String[0]);
		int minTweetLength = this.twitterToKafkaServiceConfigData.getMockMinTweetLength();
		int maxTweetLength = this.twitterToKafkaServiceConfigData.getMockMaxTweetLength();
		long sleepTimeMs = this.twitterToKafkaServiceConfigData.getMockSleepMs();

		String logMsg = Arrays.toString(keywords);
		LOG.info("Starting mock filtering for keywords: {}", logMsg);

		simulateTwitterStream(keywords, minTweetLength, maxTweetLength, sleepTimeMs);
	}

	private void simulateTwitterStream(String[] keywords, int minTweetLength, int maxTweetLength, long sleepTimeMs) {
		Executors.newSingleThreadExecutor().submit(() -> {
			try {
				while (true) {
					String formattedTweetAsRawJson = getFormattedTweet(keywords, minTweetLength, maxTweetLength);
					Status status = TwitterObjectFactory.createStatus(formattedTweetAsRawJson);
					this.twitterKafkaStatusListener.onStatus(status);
					sleep(sleepTimeMs);
				}
			}
			catch (TwitterException ex) {
				LOG.error("Error while creating status!", ex);
			}
		});
	}

	private void sleep(long sleepTimeMs) {
		try {
			Thread.sleep(sleepTimeMs);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new TwitterToKafkaServiceException("Error while sleeping for waiting new status to create!", ex);
		}
	}

	private String getFormattedTweet(String[] keywords, int minTweetLength, int maxTweetLength) {
		String[] params = new String[] {
				ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
				String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
				getRandomTweetContent(keywords, minTweetLength, maxTweetLength),
				String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)) };

		return formatTweetAsJsonWithParams(params);
	}

	private String formatTweetAsJsonWithParams(String[] params) {
		String tweet = TWEET_AS_RAW_JSON;
		for (int i = 0; i < params.length; i++) {
			tweet = tweet.replace("{" + i + "}", params[i]);
		}

		return tweet;
	}

	private String getRandomTweetContent(String[] keywords, int minTweetLength, int maxTweetLength) {
		StringBuilder tweet = new StringBuilder();
		int tweetLength = RANDOM.nextInt(maxTweetLength - minTweetLength + 1) + minTweetLength;
		return constructRandomTweet(keywords, tweetLength, tweet);
	}

	private String constructRandomTweet(String[] keywords, int tweetLength, StringBuilder tweet) {
		for (int i = 0; i < tweetLength; i++) {
			tweet.append(WORDS[RANDOM.nextInt(WORDS.length)]).append(" ");
			if (i == tweetLength / 2) {
				tweet.append(keywords[RANDOM.nextInt(keywords.length)]).append(" ");
			}
		}
		return tweet.toString().trim();
	}

}
