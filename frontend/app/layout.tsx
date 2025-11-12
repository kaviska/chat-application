import type { Metadata } from 'next'
import './globals.css'
import { ChatProvider } from '@/lib/context'

export const metadata: Metadata = {
  title: 'Admin-Member Chat Application',
  description: 'Real-time chat between admins and members',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>
        <ChatProvider>
          {children}
        </ChatProvider>
      </body>
    </html>
  )
}
