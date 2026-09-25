from PIL import Image, ImageEnhance, ImageFile
from concurrent.futures import ThreadPoolExecutor
import time

# Allow Pillow to read slightly truncated JPEG files
ImageFile.LOAD_TRUNCATED_IMAGES = True

INPUT_IMAGE = "input.jpg"
OUTPUT_IMAGE = "output.jpg"


def process_part(part_number, box, image_data):
    """Process one part of the image in a separate thread."""

    print(f"Thread {part_number} started")

    try:
        # Create a separate image from the loaded image data
        image = Image.frombytes(
            "RGB",
            image_data["size"],
            image_data["data"]
        )

        # Crop the assigned section
        part = image.crop(box)

        # Example image processing:
        # Increase brightness slightly
        enhancer = ImageEnhance.Brightness(part)
        part = enhancer.enhance(1.2)

        # Simulate some processing time
        time.sleep(1)

        image.close()

        print(f"Thread {part_number} completed")

        return part_number, box, part

    except Exception as e:
        print(f"Thread {part_number} error: {e}")
        raise


def main():

    print("Loading image...")

    try:
        # Open the input image
        image = Image.open(INPUT_IMAGE)

        # Convert to RGB and fully load the image
        image = image.convert("RGB")
        image.load()

    except Exception as e:
        print(f"Error loading image: {e}")
        return

    width, height = image.size

    print(f"Image size: {width} x {height}")

    # Store the complete image in memory.
    # This prevents multiple threads from accessing
    # the same file object.
    image_data = {
        "size": image.size,
        "data": image.tobytes()
    }

    image.close()

    # Divide image into four parts
    boxes = [
        # Top-left
        (0, 0, width // 2, height // 2),

        # Top-right
        (width // 2, 0, width, height // 2),

        # Bottom-left
        (0, height // 2, width // 2, height),

        # Bottom-right
        (width // 2, height // 2, width, height)
    ]

    print()
    print("Starting 4 threads...")
    print()

    start_time = time.time()

    # Create four threads
    with ThreadPoolExecutor(max_workers=4) as executor:

        futures = []

        for i, box in enumerate(boxes, start=1):

            future = executor.submit(
                process_part,
                i,
                box,
                image_data
            )

            futures.append(future)

        # Get results
        results = []

        for future in futures:
            result = future.result()
            results.append(result)

    print()
    print("All threads completed.")

    # Create empty output image
    output = Image.new(
        "RGB",
        (width, height)
    )

    # Combine the four processed parts
    for part_number, box, part in results:

        output.paste(
            part,
            (box[0], box[1])
        )

        part.close()

    # Save final image
    output.save(
        OUTPUT_IMAGE,
        "JPEG",
        quality=95
    )

    end_time = time.time()

    print()
    print("--------------------------------")
    print("Image processing completed!")
    print("--------------------------------")
    print(f"Input image  : {INPUT_IMAGE}")
    print(f"Output image : {OUTPUT_IMAGE}")
    print(f"Image size   : {width} x {height}")
    print(f"Threads used : 4")
    print(f"Time taken   : {end_time - start_time:.2f} seconds")
    print("--------------------------------")


if __name__ == "__main__":
    main()