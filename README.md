AsyncJobLibrary
===============

Android library to easily queue background and UI tasks

##Why AsyncJob?

In my lasts projects I started using [Android Annotations](http://androidannotations.org/), which has an amazing set of tools to make creating Android applications a lot easier.

However, for some stuff I wanted to make having to inherit from auto-built classes was an impossible thing to do and I ended up having half of my project using *AndroidAnnotations* and the other half doing stuff manually.

So I thought: maybe I should try [ButterKnife](http://jakewharton.github.io/butterknife/) instead. That worked better, I could inject views whenever I wanted and I still had some of the "@Click" stuff I liked.

But not everything was good. *AndroidAnnotations* had a pair of annotations called **@Background** and **@UIThread** which magically -well, not so magically- allowed your code to be executed on the UI Thread or on a Background one with no effort. I *REALLY* missed those annotations as they allowed me to have a way cleaner code, so I started thinking how could I replace them.

**AsyncJob** was the closest I got to that.

##So what does it exactly do?

If you are working on Android you probably have ended up using AsyncTasks to do background tasks and then have a response on the UI Thread. Well, I'll confess: **I HATE ASYNCTASKS**.

I don't see why I would need to extend a class *EVERY FUCKING TIME* I want to do some work on background. Also, having to create a Thread and a Handler *EVERY FUCKING TIME* I wanted to do some background work and have a response wasn't a good option.

So what I did is creating a library which does that for you.

##How does it work?

It's really easy. If you want the library to work in a similar way to *AsyncTask*, you can create an ``AsyncJob<JobResult>`` where **JobResult** is the type or class of the item it will return.

But creating an **AsyncJob** was also a boring thing to do so I created an ``AsyncJobBuilder<T>`` which allows you to create **AsyncJobs** in a fast and clean way.

**AsyncJobs** have two interfaces which will be used to store your code and execute it on backgrund or on the main thread. These are
``AsyncAction<ActionResult>`` and ``
AsyncResultAction<ActionResult>``. They can be set by:

``java
asyncJob.setActionInBackground(actionInBackground);
asyncJob.setActionOnResult(actionOnMainThread);
``

And when you use ``asyncJob.start()`` it will call those interfaces and execute your code.

Here you have an example of an **AsyncJob** created by an **AsyncJobBuilder**:

```java
new AsyncJob.AsyncJobBuilder<Boolean>()
        .doInBackground(new AsyncJob.AsyncAction<Boolean>() {
            @Override
            public Boolean doAsync() {
                // Do some background work
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }
        })
        .doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                Toast.makeText(context, "Result was: " + result, Toast.LENGTH_SHORT).show();
        }
}).create().start();

```

###AsyncJob static methods

Most of the time, though, I will prefer doing the following:

* Execute some code in background.
* Execute some code on the UI thread from that background thread whenever I have to, not just to return a value.

So how do yo do that?

You use the provided static methods, ``AsyncJob.doInBackground()`` and ``AsyncJob.doOnMainThread()``:

```java
AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
    @Override
    public void doOnBackground() {

        // Pretend it's doing some background processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create a fake result (MUST be final)
        final boolean result = true;

        // Send the result to the UI thread and show it on a Toast
        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
            @Override
            public void doInUIThread() {
                Toast.makeText(context, "Result was: "+ result, Toast.LENGTH_SHORT).show();
            }
        });
    }
});

```

Which also have some interfaces made specially for them.

##That's good, but I'd like to have a better control of my background threads!

Well, you can. You can provide an ```ExecutorService``` to an **AsyncJob** and the tasks that you want will be queued to it:

```java
// Create a job to run on background
AsyncJob.OnBackgroundJob job = new AsyncJob.OnBackgroundJob() {
    @Override
    public void doOnBackground() {
        // Pretend to do some background processing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // This toast should show a difference of 1000ms between calls
        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
            @Override
            public void doInUIThread() {
                Toast.makeText(context, "Finished on: "+System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
            }
        });
    }
};

// This ExecutorService will run only a thread at a time
ExecutorService executorService = Executors.newSingleThreadExecutor();

// Send 5 jobs to queue which will be executed one at a time
for(int i = 0; i < 5; i++) {
    AsyncJob.doInBackground(job, executorService);
}
```

In this example, I am supplying a SingleThreadExecutor to the AsyncJob, which will only allow **one** thread to run at a time, serializing their execution. You can provide any other ExecutorServices tof it your needs.

##How do I add it to my project?

For now, you must manually download or clone this repo and import it to your current project as a Module.

A maven repo is coming and should be enabled soon, though.

##License

This software is licensed under Apachev2 which basically means that you can make your own version and it can be private.

Here is a small summary of the license:

```
Copyright 2014 Jorge Martín Espinosa (Arasthel)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

```

##About me:

I'm a freelance Android developer which can also code some Objective-C, Swift and little web (RoR, Django, even some NodeJS...). You can find more about me here. Bad thing is: I'm Spanish and I mostly speak Spanish on those sites. Anyway, you can contact me writing in English if you need help or want to talk.

[![Twitter Account](https://dev.twitter.com/sites/default/files/images_documentation/bird_black_48_0.png)](https://twitter.com/arasthel92) [![LinkedIn](https://dlc1-s.licdn.com/sites/default/files/InBug-60px-R.png)](https://www.linkedin.com/profile/view?id=168652113)
[![Ghost Blog](http://i.imgur.com/HBgKOjh.png)](http://blog.arasthel.com)
