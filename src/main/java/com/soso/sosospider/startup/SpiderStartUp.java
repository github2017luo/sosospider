package com.soso.sosospider.startup;

import com.soso.sosospider.service.AsyncService;
import com.soso.sosospider.service.spiderService;
import com.soso.sosospider.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author cpeter
 * spider enter
 */
@Service
public class SpiderStartUp {
	@Autowired
	private AsyncService asyncService;
	@Autowired
	private spiderService spiderService;
	@Autowired
	private com.soso.sosospider.dao.redisDao redisDao;

	public boolean startUpWebSpider(String webAddr){
		boolean result=true;
		long startTime=System.currentTimeMillis();   //获取开始时间
		try{
//			first，单次爬取首页的所有链接入redis,seed为起始链接
			String seed=webAddr;

			MD5Utils md5Utils=new MD5Utils();
			String key=md5Utils.md5(webAddr);
			redisDao.save(key,"1");
			spiderService.geturl(seed,webAddr);
//          sencond,AsyncSpider Task running
			while (true) {
				for (int i = 0; i < 4; i++) {
					String url = redisDao.rpop(seed);
					String keyexist = md5Utils.md5(url);
					redisDao.save(keyexist, "1");
					asyncService.task(seed, url);
				}
				TimeUnit.SECONDS.sleep(4);
			}
		}catch (Exception e){
			System.out.println(e);
		}
		long endTime=System.currentTimeMillis(); //获取结束时间
		System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
		return result;
	}
	@PostConstruct
	public void init() {
		SpiderStartUp spiderStartUp = new SpiderStartUp();
		spiderStartUp=this;
		spiderStartUp.asyncService = this.asyncService;
	}
}
