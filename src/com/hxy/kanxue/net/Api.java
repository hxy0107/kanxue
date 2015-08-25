package com.hxy.kanxue.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hxy.kanxue.util.CookieStorage;
import com.hxy.kanxue.util.ObjStorage;
import com.hxy.kanxue.util.SimpleHASH;
import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;

import java.net.PortUnreachableException;
import java.util.List;

/**
 * Created by xianyu.hxy on 2015/8/24.
 */
public class Api {
    public static final String DOMAIN = "http://bbs.pediy.com";
    public static final String PATH = "/";
    /**
     * ��ѩ��׿�ͻ���ר�õ�ģ��id����ͨ���ڿ�ѩ��һurl�����ģ������鿴���ӿڷ�������
     */
    public static final String STYLE = "styleid=12";

    // ��¼����״̬��
    public static final int LOGIN_SUCCESS = 0;
    public static final int LOGIN_FAIL_LESS_THAN_FIVE = 1; // �û��������������5������
    public static final int LOGIN_FAIL_MORE_THAN_FIVE = 2; // ��¼ʧ�ܴ�������5��,15���Ӻ�ſɼ�����¼

    // ����������״̬��
    public static final int NEW_POST_SUCCESS = 0;
    public static final int NEW_POST_FAIL_WITHIN_THIRTY_SECONDS = 1; // ��ʮ���ڷ�������
    public static final int NEW_POST_FAIL_WITHIN_FIVE_MINUTES = 2; // 5�����ڷ���ͬ����
    public static final int NEW_POST_FAIL_NOT_ENOUGH_KX = 3; // ��ѩ�Ҳ���

    // һЩ����id��
    public static final int HELP_FORUM_ID = 20; // ��ѩ�����ʴ����id
    // public static final int SOFTWARE_DEBUG_FORUM_ID = 4; //������԰���id
    public static final int GET_JOB_FORUM_ID = 47; // ��Ƹ���id
    public static final int POST_CONTENT_SIZE_MIN = 6; // �������������С����
    public static final int NEW_FORUM_ID = 153; // �������ϰ��id
    public static final int LIFE_FORUM_ID = 45; // �����������id
    public static final int SECURITY_FORUM_ID = 61; // �����������id

    // �����ö�����
    public static final int GLOBAL_TOP_FORUM = -1;
    public static final int AREA_TOP_FORUM = 116;
    public static final int TOP_FORUM = 1;

    public static final int ALLOW_LOGIN_USERNAME_OR_PASSWD_ERROR_NUM = 5;

    private static Api mInstance = null;
    private String mToken = "guest";
    // TODO ʹ����cookieʱ����̰߳�ȫ
    private CookieStorage mCookieStorage = null;
    private SharedPreferences mPreferences = null;

    public static Api getInstance() {
        if (mInstance == null) {
            mInstance = new Api();
        }
        return mInstance;
    }

    /**
     * ����context����ʼ����������ص�����
     *
     * @param con
     *            Ҫ���õ� context
     */
    public void setmCon(Context con) {
        if (con == null)
            return;
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(con);
        this.mCookieStorage = new CookieStorage(new ObjStorage(
                this.mPreferences));
        this.getForumToken();
    }

    /**
     * @return ��ѩ��ȫ��̳��cookie�洢������
     */
    public CookieStorage getCookieStorage() {
        return this.mCookieStorage;
    }

    /**
     * ��ȡsecuritytoken��securitytoken����post����ʱ��Ҫ�ύ�����ڵ�¼״̬�õ�����token��
     * �ǵ�¼״̬�õ��ַ���guest
     */
    private void getForumToken() {
        String url = DOMAIN + PATH + "getsecuritytoken.php?" + STYLE;
        HttpClientUtil hcu = new HttpClientUtil(url, HttpClientUtil.METHOD_GET,
                new HttpClientUtil.NetClientCallback() {

                    @Override
                    public void execute(int status, String response,
                                        List<Cookie> cookies) {

                        if (status != HttpClientUtil.NET_SUCCESS)
                            return;

                        JSONObject obj = JSON.parseObject(response);
                        mToken = obj.getString("securitytoken");
                    }

                });
        if (this.isLogin()) {
            hcu.addCookie(this.mCookieStorage.getCookies());
        }
        hcu.asyncConnect();
    }

    /**
     * securitytoken��setter�ӿ�
     *
     * @param token
     */
    public void setToken(String token) {
        if (token == null)
            return;
        this.mToken = token;
    }

    /**
     * @return ���ڵ�¼״̬����true����֮����false
     */
    public boolean isLogin() {
        return this.mCookieStorage.hasCookie("bbsessionhash");
    }

    /**
     * ��ȡ��ѩ�İ���б�
     *
     * @param callback
     */
    public void getForumHomePage(final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "index.php?" + STYLE;
        HttpClientUtil hcu = new HttpClientUtil(url, HttpClientUtil.METHOD_GET,
                callback);
        if (this.isLogin()) {
            hcu.addCookie(this.mCookieStorage.getCookies());
        }
        hcu.asyncConnect();
    }

    /**
     * ��ȡָ�����������б�
     *
     * @param id
     *            ���id
     * @param page
     *            ��������б��ҳ��
     * @param callback
     */
    public void getForumDisplayPage(int id, int page,
                                    final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "forumdisplay.php?" + STYLE + "&f=" + id
                + "&page=" + page + "&order=desc";
        HttpClientUtil hcu = new HttpClientUtil(url, HttpClientUtil.METHOD_GET,
                callback);
        if (this.isLogin()) {
            hcu.addCookie(this.mCookieStorage.getCookies());
        }
        hcu.asyncConnect();
    }

    /**
     * ��ȡָ�������е������б�
     *
     * @param id
     *            ����id
     * @param page
     *            ���������б�ҳ��
     * @param callback
     */
    public void getForumShowthreadPage(int id, int page,
                                       final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "showthread.php?" + STYLE + "&t=" + id
                + "&page=" + page;
        HttpClientUtil hcu = new HttpClientUtil(url, HttpClientUtil.METHOD_GET,
                callback);
        if (this.isLogin()) {
            hcu.addCookie(this.mCookieStorage.getCookies());
        }
        hcu.asyncConnect();
    }

    /**
     * ��ȡ�������������ݡ��������ݽϳ�ʱ����ѩĬ��ֻ�����������ݣ���ͨ���ýӿڻ�ȡ�������ݡ�
     *
     * @param id
     *            ����id
     * @param callback
     */
    public void getForumFullThread(int id, final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "showpost.php?" + STYLE + "&p=" + id;
        HttpClientUtil hcu = new HttpClientUtil(url, HttpClientUtil.METHOD_GET,
                callback);
        if (this.isLogin()) {
            hcu.addCookie(this.mCookieStorage.getCookies());
        }
        hcu.asyncConnect();
    }

    /**
     * ��¼��ѩ
     *
     * @param uname
     * @param passwd
     * @param callback
     */
    public void login(String uname, String passwd,
                      final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "login.php?do=login" + "&" + STYLE;
        HttpClientUtil hcu = new HttpClientUtil(url,
                HttpClientUtil.METHOD_POST, callback);
        hcu.addParam("vb_login_username", uname);
        hcu.addParam("do", "login");
        hcu.addParam("cookieuser", "1");
        hcu.addParam("securitytoken", "guest");
        hcu.addParam("vb_login_md5password",
                SimpleHASH.md5(this.strToEnt(passwd.trim())));
        hcu.addParam("vb_login_md5password_utf", SimpleHASH.md5(passwd.trim()));
        hcu.asyncConnect();
    }

    /**
     * ���õ�¼�û��ĸ�����Ϣ
     *
     * @param username
     *            ��¼�û��û���
     * @param id
     *            ��¼�û�id
     * @param isavatar
     *            ��¼�û��Ƿ���ͷ��
     * @param email
     *            ��¼�û�email��ַ
     */
    public void setLoginUserInfo(String username, int id, int isavatar,
                                 String email) {
        if (username == null)
            return;
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putString("username", username);
        editor.putInt("userid", id);
        editor.putInt("isavatar", isavatar);
        editor.putString("email", email);
        editor.commit();
    }

    /**
     * @return �û���
     */
    public String getLoginUserName() {
        return this.mPreferences.getString("username", null);
    }

    /**
     * @return �û�id
     */
    public int getLoginUserId() {
        return this.mPreferences.getInt("userid", -1);
    }

    /**
     * @return �û���ͷ�񷵻�true
     */
    public int getIsAvatar() {
        return this.mPreferences.getInt("isavatar", 0);
    }

    /**
     * @return �û�email��ַ
     */
    public String getEmail() {
        return this.mPreferences.getString("email", null);
    }

    /**
     * �����¼�û�������Ϣ
     */
    public void clearLoginData() {
        this.mCookieStorage.clearAll();
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.remove("username");
        editor.remove("userid");
        editor.remove("isavatar");
        editor.commit();
    }

    /**
     * �ǳ�
     *
     * @param callback
     */
    public void logout(final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "login.php?do=logout&logouthash=" + mToken
                + "&" + STYLE;
        new HttpClientUtil(url, HttpClientUtil.METHOD_GET, callback)
                .asyncConnect();
    }

    /**
     * �ظ�����
     *
     * @param id
     *            ����id
     * @param msg
     *            �ظ�����
     * @param callback
     */
    public void quickReply(int id, String msg, final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "newreply.php?" + STYLE;
        HttpClientUtil hcu = new HttpClientUtil(url,
                HttpClientUtil.METHOD_POST, callback);
        hcu.addParam("message", msg);
        hcu.addParam("t", id + "");
        hcu.addParam("fromquickreply", "1");
        hcu.addParam("do", "postreply");
        hcu.addParam("securitytoken", mToken);
        hcu.addCookie(this.mCookieStorage.getCookies());
        hcu.asyncConnect();
    }

    /**
     * ����������
     *
     * @param id
     *            ���id
     * @param subject
     *            �������
     * @param msg
     *            ��������
     * @param callback
     */
    public void newThread(int id, String subject, String msg,
                          final HttpClientUtil.NetClientCallback callback) {
        getNormalNewThread(id, subject, msg, callback).asyncConnect();
    }

    /**
     * ������������kx�ҵ�������
     *
     * @param id
     * @param subject
     * @param kxReward
     *            ����kx����ֵ
     * @param msg
     * @param callback
     */
    public void newThread(int id, String subject, String kxReward, String msg,
                          final HttpClientUtil.NetClientCallback callback) {
        HttpClientUtil hcu = getNormalNewThread(id, subject, msg, callback);
        hcu.addParam("offer_Price", kxReward);
        hcu.asyncConnect();
    }

    private HttpClientUtil getNormalNewThread(int id, String subject,
                                              String msg, final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "newthread.php?do=postthread" + "&f=" + id
                + "&" + STYLE;
        HttpClientUtil hcu = new HttpClientUtil(url,
                HttpClientUtil.METHOD_POST, callback);
        hcu.addParam("subject", subject);
        hcu.addParam("message", msg);
        hcu.addParam("securitytoken", mToken);
        hcu.addParam("f", "" + id);
        hcu.addParam("do", "postthread");
        hcu.addCookie(this.mCookieStorage.getCookies());
        return hcu;
    }

    /**
     * ��ѩ����������ӿ�
     *
     * @param name
     * @param email
     * @param msg
     * @param callback
     */
    public void feedback(String name, String email, String msg,
                         final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "sendmessage.php?do=docontactus&" + STYLE;
        HttpClientUtil hcu = new HttpClientUtil(url,
                HttpClientUtil.METHOD_POST, callback);
        hcu.addParam("name", name);
        hcu.addParam("email", email);
        hcu.addParam("message", msg);
        hcu.addParam("securitytoken", mToken);
        hcu.addParam("subject", "0");
        hcu.addParam("do", "docontactus");
        hcu.addCookie(this.mCookieStorage.getCookies());
        hcu.asyncConnect();
    }

    /**
     * ����°汾
     *
     * @param callback
     */
    public void checkUpdate(HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "mobile/android/appupdate.html";
        new HttpClientUtil(url, HttpClientUtil.METHOD_GET, callback)
                .asyncConnect();
    }

    /**
     * ���ָ������µ������б��Ƿ��и���
     *
     * @param id
     *            ���id
     * @param time
     *            �ϴ�ˢ�µ�ʱ���
     * @param callback
     */
    public void checkNewPostInForumDisplayPage(int id, long time,
                                               final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "forumdisplay.php?f=" + id
                + "&getnewpost=" + time + "&" + STYLE;
        HttpClientUtil hcu = new HttpClientUtil(url, HttpClientUtil.METHOD_GET,
                callback);
        if (this.isLogin()) {
            hcu.addCookie(this.mCookieStorage.getCookies());
        }
        hcu.asyncConnect();
    }

    /**
     * ���ָ�������µ������б��Ƿ��и���
     *
     * @param id
     *            ����id
     * @param time
     *            �ϴ�ˢ�µ�ʱ���
     * @param callback
     */
    public void checkNewPostInShowThreadPage(int id, long time,
                                             final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "showthread.php?t=" + id + "&getnewpost="
                + time + "&" + STYLE;
        HttpClientUtil hcu = new HttpClientUtil(url, HttpClientUtil.METHOD_GET,
                callback);
        if (this.isLogin()) {
            hcu.addCookie(this.mCookieStorage.getCookies());
        }
        hcu.asyncConnect();
    }

    /**
     * ��ȡ��ѩ�û�ͷ���url
     *
     * @param userId
     *            �û�id
     * @return
     */
    public String getUserHeadImageUrl(int userId) {
        return DOMAIN + PATH + "image.php?u=" + userId;
    }

    /**
     * ��ȡ��ѩ�����и���ͼƬ��url
     *
     * @param id
     *            ����id
     * @return
     */
    public String getAttachmentImgUrl(int id) {
        return DOMAIN + PATH + "attachment.php?attachmentid=" + id
                + "&thumb=1&" + STYLE;
    }

    /**
     * ��¼ǰ�û�����Ԥ����
     *
     * @param input
     *            ȥ����λ�ո���û�����
     * @return
     */
    private String strToEnt(String input) {
        String output = "";

        for (int i = 0; i < input.length(); i++) {
            int ucode = input.codePointAt(i);
            String tmp = "";

            if (ucode > 255) {
                while (ucode >= 1) {
                    tmp = "0123456789".charAt(ucode % 10) + tmp;
                    ucode /= 10;
                }

                if (tmp == "") {
                    tmp = "0";
                }

                tmp = "#" + tmp;
                tmp = "&" + tmp;
                tmp = tmp + ";";
                output += tmp;
            } else {
                output += input.charAt(i);
            }
        }
        return output;
    }

    /**
     * ���ָ���û�������Ϣ�б�
     *
     * @param id
     *            �û�id
     * @param callback
     */
    public void getUserInfoPage(int id, final HttpClientUtil.NetClientCallback callback) {
        String url = DOMAIN + PATH + "member.php?u=" + id + STYLE;
        HttpClientUtil hcu = new HttpClientUtil(url, HttpClientUtil.METHOD_GET,
                callback);
        if (this.isLogin()) {
            hcu.addCookie(this.mCookieStorage.getCookies());
        }
        hcu.asyncConnect();
    }
}












































