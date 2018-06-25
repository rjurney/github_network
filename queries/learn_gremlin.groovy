// Setup our database on top of cassandra/elasticsearch
graph = JanusGraphFactory.build().
  set("storage.backend", "cassandra").
  set("storage.hostname", "127.0.0.1").
  set("storage.cassandra.keyspace", "github_graph").
  set("index.search.backend", "elasticsearch").
  open()

// Get a graph traverser
g = graph.traversal()

g.V().has('code','DFW').as('from').out().has('region','US-CA').as('to').
  select('from','to').by('code')

g.V().
  hasLabel('user').
  where(
    outE('forked').
    count().
    is(
      gt(50)
    )
  ).
  count()

g.V().
  hasLabel('repo').
  as('repo1').
  in('forked').
  has(
    'user',
    'fan_degree',
    gt(5)
  ).
  where(
    outE('forked').
      count().
      is(
        lt(100)
      )
  ).
  count()

g.V().
  hasLabel('repo').
  group().
  by('repoName').
  as('repo').
  by(
    inE('co_forked').
      count()
  ).
  as('degree').
  select('repo','degree').
  order().
  by(
    select('degree'),
    decr
  ).
  limit(20)

:plugin use tinkerpop.hadoop
:plugin use tinkerpop.spark
// I edited the keyspace in this file to github_graph
graph = GraphFactory.open('conf/hadoop-graph/read-cassandra-3.properties')

g = graph.traversal().withComputer(SparkGraphComputer)

g.withSack(0).V().hasLabel('repo').store("x").repeat(both('co_forked').simplePath()).emit().path().
           group().by(project("a","b").by(limit(local, 1)).
                                       by(tail(local, 1))).
                   by(order().by(count(local))).
                   select(values).as("shortestPaths").
                   select("x").unfold().as("v").
                   select("shortestPaths").
                     map(unfold().filter(unfold().where(eq("v"))).count()).
                     sack(sum).sack().as("betweeness").
                   select("v","betweeness").
                   order().
                   by("betweeness", decr)
                   .limit(20)

// 6
g.V().
  hasLabel('repo').
  group().
  by('repoName').
  as('repo').
  by(
    inE('co_forked').
      count()
  ).
  as('degree').
  select('repo','degree').
  order().
  by(
    select('degree'),
    decr
  ).
limit(20)

// 5
c = g.V().
  hasLabel('repo').
  as('repo1').
  in('forked').
  has(
    'user',
    'fan_degree',
    gt(5)
  ).
  where(
    outE('forked').
    count().
    is(
      lt(100)
    )
  ).
  count().
  next()
println(c)

g.V().
  hasLabel('user').
  repeat(
    groupCount('m').
      by('userName').
      out('owned').
      in('forked').
      timeLimit(10000)
  ).
  times(5).
  cap('m').
  order(local).
  by(values, decr).
  limit(local, 20).
  next()

g.V().
  hasLabel('repo').
  repeat(
    groupCount('m').
    by('repoName').
    in('forked').
    out('forked').
    timeLimit(30000)
  ).
  times(25).
  cap('m').
  order(local).
  by(values, decr).
  limit(local, 20).
  next()
  
==>spring-projects/spring-framework=2458977775590865782
==>tensorflow/tensorflow=2348175220939948032
==>kubernetes/kubernetes=2348175220939948032
==>fchollet/keras=1956812684116623360
==>JeffLi1993/springboot-learning-example=1956812684116623360
==>changmingxie/tcc-transaction=1565450147293298688
==>justjavac/free-programming-books-zh_CN=1565450147293298688
==>BVLC/caffe=1565450147293298688
==>ctripcorp/apollo=1565450147293298688
==>apache/hadoop=1565450147293298688
==>gitlabhq/gitlabhq=1565450147293298688
==>golang/go=1174087610469974016
==>apache/incubator-mxnet=1174087610469974016
==>naver/pinpoint=1174087610469974016
==>apache/activemq=782725073646649344
==>vespa-engine/vespa=782725073646649344
==>liuyangming/ByteTCC=782725073646649344
==>Tencent/MSEC=782725073646649344
==>OpenSkywalking/skywalking=782725073646649344
==>grpc/grpc-java=782725073646649344

g.V().
  hasLabel('repo').
  as('repo').
  repeat(
    groupCount('m').
    by('repoName').
    in('forked').
    out('forked').
    where(neq('repo')).
    timeLimit(30000)
  ).
  times(10).
  cap('m').
  order(local).
  by(values, decr).
  limit(local, 20).
  next()

==>spring-projects/spring-framework=2344863886509430
==>spring-projects/spring-boot=442714581230976
==>tensorflow/tensorflow=98381018051328
==>kubernetes/kubernetes=98381018051328
==>fchollet/keras=81984181709440
==>JeffLi1993/springboot-learning-example=81984181709440
==>changmingxie/tcc-transaction=65587345367552
==>justjavac/free-programming-books-zh_CN=65587345367552
==>ctripcorp/apollo=65587345367552
==>apache/hadoop=65587345367552
==>gitlabhq/gitlabhq=65587345367552
==>golang/go=49190509025664
==>apache/incubator-mxnet=49190509025664
==>naver/pinpoint=49190509025664
==>apache/activemq=32793672683776
==>vespa-engine/vespa=32793672683776
==>liuyangming/ByteTCC=32793672683776
==>Tencent/MSEC=32793672683776
==>OpenSkywalking/skywalking=32793672683776
==>grpc/grpc-java=32793672683776

g.V().
  project('repo', 'degree')
  hasLabel('repo').
  group().
  by().
  by(
    inE('forked').
    count()
  ).
  order().
  by(
    select("degree"),
    decr
  ).
  limit(4, local)

g.V().
  hasLabel('repo').
  repeat(
    groupCount('m').
    by('repoName').
    as('repo1').
    in('forked').
    out('forked').
    where(neq('repo1'))
  ).
  times(5).
  cap('m').
  order(local).
  by(values, decr).
  limit(local, 20).
  next()

g.V().
  hasLabel('repo').
  as('repo').
  repeat(
    inE('forked').
    outV().
    outE('forked').
    inV().
    where(
      neq('repo')
    ).
    groupCount('m').
    by('repoName').
    timeLimit(60000)
  ).
  times(5).
  cap('m').
  order(local).
  by(values, decr).
  limit(local, 50).
  next()

==>spring-projects/spring-framework=174483355
==>spring-projects/spring-boot=35030041
==>alibaba/dubbo=19477617
==>mybatis/mybatis-3=13503659
==>iluwatar/java-design-patterns=11075447
==>google/guava=10730678
==>shuzheng/zheng=10661385
==>alibaba/druid=8599261
==>netty/netty=8131501
==>twbs/bootstrap=7464123
==>elastic/elasticsearch=7387712
==>alibaba/fastjson=7376062
==>tensorflow/tensorflow=6652164
==>judasn/IntelliJ-IDEA-Tutorial=5902099
==>justjavac/free-programming-books-zh_CN=4997566
==>apache/tomcat=4849310
==>apache/kafka=4823756
==>apache/spark=4805576
==>apache/zookeeper=4659028
==>ReactiveX/RxJava=4636029
==>vuejs/vue=4621552

    
g.V().
  hasLabel('repo').
  as('repo').
  group().
  by('repoName').
  by(
    inE('starred').
    count()
  ).
  order(local).
  by(values, decr).
  limit(50).
  next()

==>freeCodeCamp/freeCodeCamp=85911
==>tensorflow/tensorflow=46062
==>vuejs/vue=43256
==>facebook/react=31511
==>mr-mig/every-programmer-should-know=30568
==>kamranahmedse/developer-roadmap=29921
==>sindresorhus/awesome=27214
==>getify/You-Dont-Know-JS=26945
==>thedaviddias/Front-End-Checklist=24348
==>facebookincubator/create-react-app=23860
==>donnemartin/system-design-primer=22204
==>GoogleChrome/puppeteer=22052
==>Microsoft/vscode=21681
==>sdmg15/Best-websites-a-programmer-should-visit=21397
==>jwasham/coding-interview-university=20854
==>toddmotto/public-apis=20573
==>airbnb/javascript=20249
==>kamranahmedse/design-patterns-for-humans=19177
==>twbs/bootstrap=18890
==>robbyrussell/oh-my-zsh=18344
==>airbnb/lottie-android=18260
==>vuejs/awesome-vue=18030
==>tipsy/github-profile-summary=17445
==>facebook/react-native=17326
==>ryanmcdermott/clean-code-javascript=17104
==>kdn251/interviews=17078
==>mzabriskie/axios=17059
==>vinta/awesome-python=16768
==>electron/electron=16491
==>tensorflow/models=16372
==>FreeCodeCampChina/freecodecamp.cn=16317
==>python/cpython=16038
==>yangshun/tech-interview-handbook=15980
==>github/gitignore=15975
==>Hack-with-Github/Awesome-Hacking=15825
==>k88hudson/git-flight-rules=15774
==>ElemeFE/element=15611
==>torvalds/linux=15277
==>nodejs/node=15263
==>bailicangdu/vue2-elm=15132
==>mbeaudru/modern-js-cheatsheet=15087
==>EbookFoundation/free-programming-books=14762
==>d3/d3=14511
==>golang/go=14280
==>webpack/webpack=14220
==>prettier/prettier=14080
==>parcel-bundler/parcel=14008
==>JetBrains/kotlin=13898
==>zeeshanu/learn-regex=13801
==>angular/angular=13692
==>bitcoin/bitcoin=13663
==>Chalarangelo/30-seconds-of-code=13422
==>zeit/next.js=13305
==>vhf/free-programming-books=13289
==>ant-design/ant-design=13189
==>fchollet/keras=12856
==>exacity/deeplearningbook-chinese=12833
==>jgthms/bulma=12679
==>iluwatar/java-design-patterns=12604
==>Microsoft/TypeScript=12568
==>airbnb/lottie-ios=12198
==>lkzhao/Hero=11860
==>kubernetes/kubernetes=11741
==>aymericdamien/TensorFlow-Examples=11664
==>graphcool/chromeless=11661
==>wearehive/project-guidelines=11558
==>justjavac/free-programming-books-zh_CN=11408
==>i0natan/nodebestpractices=11404
==>atom/atom=11393
==>getlantern/forum=11377
==>colebemis/feather=11230
==>resume/resume.github.com=11169
==>tootsuite/mastodon=11112
==>getlantern/lantern=11082
==>reactjs/redux=11081
==>daneden/animate.css=11080
==>google/guetzli=11000
==>oxford-cs-deepnlp-2017/lectures=10973
==>pytorch/pytorch=10958
==>developit/preact=10857
==>facebook/prepack=10855
==>googlesamples/android-architecture=10837
==>rg3/youtube-dl=10835
==>josephmisiti/awesome-machine-learning=10827
==>gothinkster/realworld=10788
==>shadowsocks/shadowsocks-windows=10715
==>minimaxir/big-list-of-naughty-strings=10604
==>terryum/awesome-deep-learning-papers=10525
==>spring-projects/spring-boot=10504
==>airbnb/react-sketchapp=10444
==>ReactiveX/RxJava=10429
==>nvbn/thefuck=10413
==>shieldfy/API-Security-Checklist=10392
==>laravel/laravel=10295
==>denysdovhan/wtfjs=10231
==>papers-we-love/papers-we-love=10207
==>gatsbyjs/gatsby=10107
==>keon/algorithms=10107
==>typicode/json-server=10079
==>frappe/charts=9998

g.V().
  hasLabel('repo').
  as('repo').
  group().
  by('repoName').
  by(
    inE('forked').
    count()
  ).
  order(local).
  by(values, decr).
  limit(50).
  next()

==>tensorflow/tensorflow=26206
==>jtleek/datasharing=21548
==>SmartThingsCommunity/SmartThingsPublic=19162
==>octocat/Spoon-Knife=17022
==>twbs/bootstrap=15249
==>rdpeng/ProgrammingAssignment2=13790
==>github/gitignore=11643
==>barryclark/jekyll-now=11338
==>tensorflow/models=11222
==>vuejs/vue=8885
==>LarryMad/recipes=8627
==>facebook/react=8048
==>spring-projects/spring-boot=7685
==>jlord/patchwork=7349
==>jwasham/coding-interview-university=7336
==>udacity/frontend-nanodegree-resume=7133
==>facebookincubator/create-react-app=6759
==>hasura/imad-app=6656
==>getify/You-Dont-Know-JS=6568
==>opencv/opencv=6233
==>torvalds/linux=6163
==>lord/slate=6000
==>rdpeng/ExData_Plotting1=5945
==>freeCodeCamp/freeCodeCamp=5786
==>bitcoin/bitcoin=5744
==>fchollet/keras=5582
==>nightscout/cgm-remote-monitor=5462
==>DefinitelyTyped/DefinitelyTyped=5319
==>bailicangdu/vue2-elm=5284
==>laravel/laravel=5220
==>reactjs/redux=5201
==>ant-design/ant-design=5189
==>mrdoob/three.js=5121
==>django/django=5088
==>kubernetes/kubernetes=5057
==>nodejs/node=5021
==>facebook/react-native=4961
==>aymericdamien/TensorFlow-Examples=4888
==>codeschool-projects/HelloCodeSchoolProject=4870
==>scikit-learn/scikit-learn=4855
==>apache/spark=4808
==>racaljk/hosts=4727
==>wesbos/JavaScript30=4646
==>alibaba/dubbo=4625
==>EbookFoundation/free-programming-books=4555
==>eugenp/tutorials=4554
==>airbnb/javascript=4553
==>sindresorhus/awesome=4502
==>python/cpython=4399
==>angular/angular.js=4371
==>spring-projects/spring-framework=4324
==>d3/d3=4291
==>udacity/ud851-Exercises=4261
==>ansible/ansible=4252
==>kamranahmedse/developer-roadmap=4229
==>openshift/nodejs-ex=4200
==>rdpeng/RepData_PeerAssessment1=4155
==>angular/angular=4092
==>udacity/fullstack-nanodegree-vm=4088
==>BVLC/caffe=4081
==>mmistakes/minimal-mistakes=4055
==>iluwatar/java-design-patterns=4029
==>jquery/jquery=3854
==>atom/atom=3833
==>vhf/free-programming-books=3806
==>exacity/deeplearningbook-chinese=3802
==>git/git=3789
==>robbyrussell/oh-my-zsh=3743
==>udacity/create-your-own-adventure=3711
==>shadowsocks/shadowsocks-windows=3660
==>shuzheng/zheng=3644
==>vinta/awesome-python=3627
==>googlesamples/android-architecture=3614
==>aspnet/Docs=3593
==>Microsoft/vscode=3572
==>almasaeed2010/AdminLTE=3567
==>elastic/elasticsearch=3539
==>ElemeFE/element=3535
==>justjavac/free-programming-books-zh_CN=3501
==>pallets/flask=3407
==>electron/electron=3405
==>learn-co-students/javascript-intro-to-functions-lab-bootcamp-prep-000=3384
==>shadowsocks/shadowsocks=3337
==>Kallaway/100-days-of-code=3318
==>vuejs/vuex=3288
==>freeCodeCamp/guides=3215
==>learn-co-students/js-from-dom-to-node-bootcamp-prep-000=3115
==>udacity/ud851-Sunshine=3052
==>rails/rails=3034
==>learn-co-students/js-functions-lab-bootcamp-prep-000=3023
==>ServiceNow/devtraining-needit-istanbul=2984
==>Blankj/AndroidUtilCode=2908
==>ServiceNow/devtraining-needit-jakarta=2900
==>vuejs/awesome-vue=2881
==>daneden/animate.css=2874
==>ruanyf/es6tutorial=2870
==>google/protobuf=2868
==>learn-co-students/javascript-arithmetic-lab-bootcamp-prep-000=2824
==>airbnb/lottie-android=2805
==>learn-co-students/js-if-else-files-lab-bootcamp-prep-000=2769