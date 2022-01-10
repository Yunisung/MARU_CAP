package com.pgmate.cap.main;

import com.pgmate.lib.util.lang.CommonUtil;

/**
 * @author Administrator
 *
 */
public class CapUtil {

	/**
	 * 
	 */
	public CapUtil() {
		// TODO Auto-generated constructor stub
	}
	
	public static String getIssuer(String str){
		
		if(CommonUtil.isNullOrSpace(str)){
			return "기타";
		}else{
			str = str.replaceAll("카드","").replaceAll("은행", "");
		}
		
		if(str.indexOf("롯데") > -1){
			return "롯데";
		}else if(str.indexOf("현대") > -1){
			return "현대";
		}else if(str.indexOf("국민") > -1 || str.indexOf("KB") > -1){
			return "국민";
		}else if(str.indexOf("비씨") > -1 || str.indexOf("BC") > -1 || str.indexOf("케이") > -1){
			return "비씨";
		}else if(str.indexOf("하나") > -1 || str.indexOf("SK") > -1 || str.indexOf("외환") > -1){
			return "하나";
		}else if(str.indexOf("농협") > -1 || str.indexOf("NH") > -1){
			return "농협";
		}else if(str.indexOf("삼성") > -1){
			return "삼성";
		}else if(str.indexOf("신한") > -1){
			return "신한";
		}else if(str.indexOf("우리") > -1){
			return "우리";
		}else if(str.indexOf("전북") > -1){
			return "전북";
		}else if(str.indexOf("수협") > -1){
			return "수협";
		}else if(str.indexOf("씨티") > -1){
			return "씨티";
		}else if(str.indexOf("제주") > -1){
			return "제주";
		}else if(str.indexOf("카카오") > -1){
			return "국민";
		}else if(str.indexOf("우체국") > -1){
			return "우체국";
		}else if(str.indexOf("광주") > -1){
			return "광주";
		}else if(str.indexOf("한미") > -1){
			return "한미";
		}else if(str.indexOf("MG") > -1 || str.indexOf("새마을") > -1){
			return "새마을";
		}else if(str.indexOf("SC") > -1 || str.indexOf("제일") > -1){
			return "SC제일";
		}else if(str.indexOf("산업") > -1 || str.indexOf("KDB") > -1){
			return "산업";
		}else if(str.indexOf("신용") > -1 || str.indexOf("신협") > -1){
			return "신협";
		}else if(str.indexOf("IBK") > -1 || str.indexOf("기업") > -1){
			return "기업";
		}else if(str.indexOf("대구") > -1 || str.indexOf("DG") > -1){
			return "대구";
		}else if(str.indexOf("경남") > -1 || str.indexOf("BNK") > -1){
			return "경남";
		}else if(str.indexOf("부산") > -1 ){
			return "부산";
		}else if(str.indexOf("해외") > -1 ){
			return "해외";
		}else{
			return str;
		}
	}
	
	public static String getAcquirer(String str){
		
		if(CommonUtil.isNullOrSpace(str)){
			return "기타";
		}else{
			str = str.replaceAll("카드","").replaceAll("은행", "");
		}
		
		if(str.indexOf("NH") > -1 || str.indexOf("농협") > -1 ){
			return "농협";
		}else if(str.indexOf("KB") > -1 || str.indexOf("국민") > -1){
			return "국민";
		}else if(str.indexOf("현대") > -1 ){
			return "현대";
		}else if(str.indexOf("삼성") > -1 || str.indexOf("아맥스") > -1 || str.indexOf("아멕스") > -1 ){
			return "삼성";
		}else if(str.indexOf("신한") > -1 ){
			return "신한";
		}else if(str.indexOf("비씨") > -1 || str.indexOf("BC") > -1 || str.indexOf("은련") > -1 ){
			return "비씨";
		}else if(str.indexOf("하나") > -1 || str.indexOf("외환") > -1 || str.indexOf("KEB") > -1 ){
			return "하나";
		}else if(str.indexOf("롯데") > -1 ){
			return "롯데";
		}else{
			return str;
		}
	}

}
