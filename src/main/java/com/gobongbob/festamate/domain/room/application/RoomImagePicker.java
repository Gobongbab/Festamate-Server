package com.gobongbob.festamate.domain.room.application;

import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class RoomImagePicker {

    private static final List<String> IMAGE_URLS = List.of(
            "https://plus.unsplash.com/premium_photo-1681830682381-478268dcc5d6?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NXx8Y29sbGVnZSUyMGZlc3RpdmFsfGVufDB8fDB8fHww",
            "https://plus.unsplash.com/premium_photo-1681841804755-3baa70b97a6c?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTN8fGNvbGxlZ2UlMjBmZXN0aXZhbHxlbnwwfHwwfHx8MA%3D%3D",
            "https://plus.unsplash.com/premium_photo-1681830968052-1c3676fb8117?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTd8fGNvbGxlZ2UlMjBmZXN0aXZhbHxlbnwwfHwwfHx8MA%3D%3D",
            "https://plus.unsplash.com/premium_photo-1681830977092-adafd313f327?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjV8fGNvbGxlZ2UlMjBmZXN0aXZhbHxlbnwwfHwwfHx8MA%3D%3D",
            "https://images.unsplash.com/photo-1656137631992-efcfe6b23ce9?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MzJ8fGNvbGxlZ2UlMjBmZXN0aXZhbHxlbnwwfHwwfHx8MA%3D%3D",
            "https://plus.unsplash.com/premium_photo-1681830932665-8c9a53a9e731?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MzN8fGNvbGxlZ2UlMjBmZXN0aXZhbHxlbnwwfHwwfHx8MA%3D%3D",
            "https://plus.unsplash.com/premium_photo-1681830693025-42932def62f7?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NDl8fGNvbGxlZ2UlMjBmZXN0aXZhbHxlbnwwfHwwfHx8MA%3D%3D",
            "https://images.unsplash.com/photo-1576646722964-470f2b45ecfc?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NTZ8fGNvbGxlZ2UlMjBmZXN0aXZhbHxlbnwwfHwwfHx8MA%3D%3D",
            "https://plus.unsplash.com/premium_photo-1658507039120-b886e903c542?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NTd8fGNvbGxlZ2UlMjBmZXN0aXZhbHxlbnwwfHwwfHx8MA%3D%3D",
            "https://plus.unsplash.com/premium_photo-1681830943606-bfab210b17e6?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NjF8fGNvbGxlZ2UlMjBmZXN0aXZhbHxlbnwwfHwwfHx8MA%3D%3D"
    );

    private final Random random = new Random();

    public String getRandomImageUrl() {
        int index = random.nextInt(IMAGE_URLS.size());
        return IMAGE_URLS.get(index);
    }
}
