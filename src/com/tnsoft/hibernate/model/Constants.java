package com.tnsoft.hibernate.model;

public interface Constants {

	public interface State {
		public static final int STATE_PENDING = 0;
		public static final int STATE_ACTIVE = 1;
		public static final int STATE_FINISHED = 2;
		public static final int STATE_DISABLED = 3;
		public static final int STATE_DELETED = 4;
	}
	
	public interface UserState {
		public static final int STATE_NORMAL = 1;
		public static final int STATE_CANCLE = 0;
	}
	
	public interface UserRoleState {
		public static final int STATE_NORMAL = 1;
		public static final int STATE_CANCLE = 0;
	}

	public interface BinaryFileType {
		public static final int USER_ICON = 0;
		public static final int ICON = 99;
	}

	public interface MimeType {
		public static final int UNKNOWN = 0;
		public static final int IMAGE_JPEG = 1;
		public static final int IMAGE_GIF = 2;
		public static final int IMAGE_PNG = 3;
	}

	public interface ExpressState {
		public static final int STATE_PENDING = 0;
		public static final int STATE_ACTIVE = 1;
		public static final int STATE_FINISHED = 2;
	}

	public interface BindState {
		public static final int STATE_PENDING = 0;
		public static final int STATE_ACTIVE = 1;
		public static final int STATE_DELETE = 2;
	}

	public interface TagState {
		public static final int STATE_DELETE = 0;
		public static final int STATE_ACTIVE = 1;
		public static final int STATE_WORKING = 2;
		public static final int STATE_OFFLINE = 3;
		//public static final float ALERT_SLEEPTIME = 2;// 报警后设置为2分钟GSP要求
	}
	
	public interface TagBuzzerState {
		public static final int STATE_OFF = 0;
		public static final int STATE_ON = 1;
	}

	public interface DomainState {
		public static final int STATE_DElETE = 0;
		public static final int STATE_ACTIVE = 1;
		public static final int STATE_DISABLE = 2;
	}

	public interface Role {
		public static final int SUPER_ADMIN = 1;
		public static final int ADMIN = 2;
		public static final int MAINTAINER = 3;
		public static final int COURIER = 4;
		public static final int EXCHANGE_USER = 5;
	}

	public interface AlertState {
		public static final int STATE_ACTIVE = 1;
		public static final int STATE_FINISHED = 2;
	}

	public interface AlertLevel {
		public static final int STATE_SERIOUS = 1;
		public static final int STATE_NOT_RESPONSE = 2;
		public static final int STATE_NORAML_LOW = 3;
		public static final int STATE_NORAML_HIGH = 0;
	}
	
	public interface AlertType {
		public static final int STATE_NOT_RESPONSE = 0; 
		public static final int STATE_TEMPHISALERT = 1;
		public static final int STATE_ELECTRICITY = 2;
		public static final int STATE_BUZZER = 3;
		public static final int STATE_PRICISION = 4;
	}
}
