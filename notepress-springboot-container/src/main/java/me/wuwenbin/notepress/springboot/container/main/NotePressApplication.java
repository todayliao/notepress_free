package me.wuwenbin.notepress.springboot.container.main;

import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wuwenbin
 */
@Slf4j
@SpringBootApplication
@MapperScan(basePackages = "me.wuwenbin", annotationClass = MybatisMapper.class)
@EnableScheduling
@EnableCaching
@ComponentScan({"me.wuwenbin"})
public class NotePressApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotePressApplication.class, args);
        log.info("处理完毕，NotePress 启动成功！ヾ(^∀^)ﾉ");
    }

}
