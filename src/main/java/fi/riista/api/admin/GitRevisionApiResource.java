package fi.riista.api.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.rossillo.spring.web.mvc.CacheControl;
import net.rossillo.spring.web.mvc.CachePolicy;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/api/revision")
@PreAuthorize("isAuthenticated() and hasRole('ROLE_ADMIN')")
public class GitRevisionApiResource {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class GitRepositoryState {
        public String branch;                  // =${git.branch}
        public String describe;                // =${git.commit.id.describe}
        public String commitId;                // =${git.commit.id}
        public String buildUserName;           // =${git.build.user.name}
        public String buildUserEmail;          // =${git.build.user.email}
        public String buildTime;               // =${git.build.time}
        public String commitUserName;          // =${git.commit.user.name}
        public String commitUserEmail;         // =${git.commit.user.email}
        public String commitMessageFull;       // =${git.commit.message.full}
        public String commitMessageShort;      // =${git.commit.message.short}
        public String commitTime;              // =${git.commit.time}

        public GitRepositoryState() {
        }

        public GitRepositoryState(Environment env) {
            this.branch = env.getProperty("git.branch");
            this.describe = env.getProperty("git.commit.id.describe");
            this.commitId = env.getProperty("git.commit.id");
            this.buildUserName = env.getProperty("git.build.user.name");
            this.buildUserEmail = env.getProperty("git.build.user.email");
            this.buildTime = env.getProperty("git.build.time");
            this.commitUserName = env.getProperty("git.commit.user.name");
            this.commitUserEmail = env.getProperty("git.commit.user.email");
            this.commitMessageShort = env.getProperty("git.commit.message.short");
            this.commitMessageFull = env.getProperty("git.commit.message.full");
            this.commitTime = env.getProperty("git.commit.time");
        }
    }

    @Resource
    private Environment env;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @CacheControl(policy = CachePolicy.NO_CACHE)
    public GitRepositoryState checkGitRevision() {
        return new GitRepositoryState(env);
    }
}
