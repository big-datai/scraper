package com.utils.aws;

import java.util.List;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;

public class AWSUtils {

	static AmazonEC2Client ec2 = new AmazonEC2Client(new BasicAWSCredentials(
			"AKIAJQUAOI7EBC6Y7ESQ", "JhremVoqNuEYG8YS9J+duW0hFRtX+sWjuZ0vdQlE"));
	static DescribeInstancesResult ad = ec2.describeInstances();
	static List<Reservation> reservations = ad.getReservations();

	public static String getPrivateIp(String publicIp) {

		String privateIp = "";
		if (publicIp == null)
			return privateIp;
		for (Reservation reservation : reservations) {
			if (reservation.getInstances().get(0).getPrivateIpAddress() != null) {
				String ip = reservation.getInstances().get(0)
						.getPublicIpAddress();
				if (publicIp.equals(ip)) {
					privateIp = reservation.getInstances().get(0)
							.getPrivateIpAddress();
				}
			}
		}
		return privateIp;
	}
}
