package com.chtrembl.petstore.order.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.core.dependencies.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

public class ContainerEnvironment implements Serializable {
  private static final Logger logger = LoggerFactory.getLogger(ContainerEnvironment.class);
  public static final String UNKNOWN = "unknown";
  private String containerHostName = null;
  private String appVersion = null;
  private String appDate = null;
  private String year = null;

  @Value("${petstore.service.app.url}")
  private String petStoreAppURL;

  @Value("${petstore.service.orderreservationfn.url}")
  private String petStoreOrderReservationFnURL;

  @PostConstruct
  private void initialize() {

    try {
      this.setContainerHostName(
        InetAddress.getLocalHost().getHostAddress() + "/" + InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException e) {
      this.setContainerHostName(UNKNOWN);
    }

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      InputStream resourcee = new ClassPathResource("version.json").getInputStream();
      String text = null;
      try (final Reader reader = new InputStreamReader(resourcee)) {
        text = CharStreams.toString(reader);
      }

      Version version = objectMapper.readValue(text, Version.class);
      this.setAppVersion(version.getVersion());
      this.setAppDate(version.getDate());
    } catch (IOException e) {
      logger.info("error parsing file {}", e.getMessage());
      this.setAppVersion(UNKNOWN);
      this.setAppDate(UNKNOWN);
    }

    this.setYear(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
  }

  public String getContainerHostName() {
    return containerHostName;
  }

  public void setContainerHostName(String containerHostName) {
    this.containerHostName = containerHostName;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public void setAppVersion(String appVersion) {
    this.appVersion = appVersion;
  }

  public String getAppDate() {
    return appDate;
  }

  public void setAppDate(String appDate) {
    this.appDate = appDate;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getAuthor() {
    return "Chris Tremblay MSFT";
  }

  public String getPetStoreAppURL() {
    return petStoreAppURL;
  }

  public void setPetStoreAppURL(String petStoreAppURL) {
    this.petStoreAppURL = petStoreAppURL;
  }

  public String getPetStoreOrderReservationFnURL() {
    return petStoreOrderReservationFnURL;
  }

  public void setPetStoreOrderReservationFnURL(String petStoreOrderReservationFnURL) {
    this.petStoreOrderReservationFnURL = petStoreOrderReservationFnURL;
  }
}
